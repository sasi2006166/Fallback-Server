package me.candiesjar.fallbackserver.handler;

import com.google.common.collect.Lists;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.ServerCacheManager;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.server.impl.FallingServerManager;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.VelocityUtils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class FallbackLimboHandler implements LimboSessionHandler {

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();
    private final FallingServerManager fallingServerManager = fallbackServerVelocity.getFallingServerManager();
    private final ServerCacheManager serverCacheManager = fallbackServerVelocity.getServerCacheManager();

    private final RegisteredServer target;
    private final UUID uuid;
    private final Player player;

    @Getter
    private ScheduledTask reconnectTask;

    @Getter
    private ScheduledTask titleTask;

    @Getter
    private ScheduledTask connectTask;

    @Override
    public void onSpawn(Limbo server, LimboPlayer limboPlayer) {
        limboPlayer.disableFalling();

        sendTitle(player, VelocityMessages.RECONNECT_TITLE, VelocityMessages.RECONNECT_SUB_TITLE);

        AtomicInteger tries = new AtomicInteger(0);

        reconnectTask = fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> {
            start(tries, limboPlayer);
        }).repeat(VelocityConfig.RECONNECT_DELAY.get(Integer.class), TimeUnit.SECONDS).schedule();
    }

    private void start(AtomicInteger tries, LimboPlayer limboPlayer) {

        VelocityUtils.printDebug("Reconnect task started for " + player.getUsername());

        boolean maxTries = tries.getAndIncrement() == VelocityConfig.RECONNECT_TRIES.get(Integer.class);

        VelocityUtils.printDebug("Tries: " + tries.get() + " | Max tries: " + maxTries);

        if (maxTries) {

            VelocityUtils.printDebug("Max tries reached for " + player.getUsername());

            boolean fallback = VelocityConfig.RECONNECT_FALLBACK.get(Boolean.class);

            if (fallback) {
                handleFallback(limboPlayer);
                return;
            }

            fallbackServerVelocity.cancelReconnect(uuid);
            limboPlayer.disconnect();
            return;
        }

        boolean useSockets = fallbackServerVelocity.isUseSockets();

        if (useSockets) {

            VelocityUtils.printDebug("Using sockets for " + player.getUsername());

            String reconnectAddress = target.getServerInfo().getAddress().getHostName();
            String translatedAddress = translateAddress(reconnectAddress);

            if (serverCacheManager.containsKey(translatedAddress)) {
                VelocityUtils.printDebug("Server cache contains " + translatedAddress + " for " + player.getUsername());
                reconnect(limboPlayer);
            }

            return;
        }

        reconnect(limboPlayer);
    }

    private void reconnect(LimboPlayer limboPlayer) {
        boolean isReachable = ping(target, VelocityConfig.RECONNECT_PING_TIMEOUT.get(Integer.class));

        VelocityUtils.printDebug("Server is reachable: " + isReachable + " for " + player.getUsername());

        if (isReachable) {
            reconnectTask.cancel();
            titleTask.cancel();

            clear();
            sendTitle(player, VelocityMessages.CONNECTING_TITLE, VelocityMessages.CONNECTING_SUB_TITLE);

            connectTask = fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> {
                limboPlayer.disconnect(target);
                clear();
                titleTask.cancel();
                VelocityUtils.printDebug("Scheduled connection");
                sendTitle(player, VelocityMessages.CONNECTED_TITLE, VelocityMessages.CONNECTED_SUB_TITLE);

                boolean clearChat = VelocityConfig.CLEAR_CHAT_RECONNECT.get(Boolean.class);

                if (clearChat) {
                    ChatUtil.clearChat(player);
                }

                fallbackServerVelocity.cancelReconnect(uuid);
            }).delay(VelocityConfig.RECONNECT_DELAY.get(Integer.class), TimeUnit.SECONDS).schedule();
        } else {
            serverCacheManager.removeIfContains(target.getServerInfo().getAddress().getHostName());
        }
    }

    private void sendTitle(Player player, VelocityMessages title, VelocityMessages subTitle) {
        AtomicInteger dots = new AtomicInteger(0);

        titleTask = fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> {
            int currentDots = dots.incrementAndGet() % 5;
            TitleUtil.sendReconnectingTitle(0, 1 + 20, currentDots, title, subTitle, player);
        }).repeat(1, TimeUnit.SECONDS).schedule();
    }

    private void handleFallback(LimboPlayer limboPlayer) {
        List<RegisteredServer> lobbies = Lists.newArrayList(fallingServerManager.getAll());

        if (lobbies.isEmpty()) {
            limboPlayer.getProxyPlayer().disconnect(Component.text(ChatUtil.getFormattedString(VelocityMessages.NO_SERVER, new Placeholder("prefix", VelocityMessages.PREFIX.get(String.class)))));
            fallbackServerVelocity.cancelReconnect(uuid);
            return;
        }

        lobbies.sort(Comparator.comparingInt(o -> o.getPlayersConnected().size()));

        RegisteredServer registeredServer = lobbies.get(0);

        limboPlayer.disconnect(registeredServer);
        fallbackServerVelocity.cancelReconnect(uuid);
        VelocityMessages.CONNECTION_FAILED.send(player, new Placeholder("prefix", VelocityMessages.PREFIX.get(String.class)));
    }

    private boolean ping(RegisteredServer registeredServer, int timeout) {
        PingOptions.Builder options = PingOptions.builder().timeout(Duration.ofMillis(timeout));

        try {
            registeredServer.ping(options.build()).get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void clear() {
        player.showTitle(Title.title(Component.empty(), Component.empty()));
        player.clearTitle();
        player.resetTitle();
    }

    private String translateAddress(String address) {
        String[] translateAddresses = {"translated1", "translated2", "translated3"};
        String[] translatedAddresses = {"127.0.0.1", "0.0.0.0", "localhost"};

        for (int i = 0; i < translateAddresses.length; i++) {
            if (address.equals(translateAddresses[i])) {
                return translatedAddresses[i];
            }
        }
        return address;
    }


}
