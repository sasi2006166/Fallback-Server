package me.candiesjar.fallbackserver.handler;

import com.google.common.collect.Lists;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.server.impl.FallingServerManager;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FallbackLimboHandler implements LimboSessionHandler {

    private final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();
    private final FallingServerManager fallingServerManager;
    private final RegisteredServer target;
    private final UUID uuid;
    private final Player player;

    @Getter
    private ScheduledTask reconnectTask;

    @Getter
    private ScheduledTask titleTask;

    public FallbackLimboHandler(RegisteredServer target, UUID uuid, Player player) {
        this.target = target;
        this.uuid = uuid;
        this.player = player;
        this.fallingServerManager = instance.getFallingServerManager();
    }

    @Override
    public void onSpawn(Limbo server, LimboPlayer limboPlayer) {
        limboPlayer.disableFalling();

        sendTitle(player);

        AtomicInteger tries = new AtomicInteger(0);

        PingOptions.Builder options = PingOptions.builder();
        options.timeout(Duration.ofMillis(VelocityConfig.RECONNECT_PING_TIMEOUT.get(Integer.class)));

        reconnectTask = instance.getServer().getScheduler().buildTask(instance, () -> {

            System.out.println("Reconnecting " + player.getUsername() + " to " + target.getServerInfo().getName());

            Optional<ServerConnection> connection = player.getCurrentServer();

            if (connection.isPresent()) {
                RegisteredServer registeredServer = connection.get().getServer();
                if (registeredServer.getServerInfo().getName().equals(target.getServerInfo().getName())) {
                    instance.cancelReconnect(uuid);
                    return;
                }
                return;
            }

            if (tries.get() == VelocityConfig.RECONNECT_TRIES.get(Integer.class)) {
                VelocityMessages.CONNECTION_FAILED.send(player, new Placeholder("prefix", VelocityMessages.PREFIX.get(String.class)));

                boolean fallback = VelocityConfig.RECONNECT_SORT.get(Boolean.class);

                if (fallback) {
                    handleFallback(limboPlayer);
                    return;
                }

                instance.cancelReconnect(uuid);
                limboPlayer.disconnect();
                return;
            }

            target.ping(options.build()).whenComplete((result, error) -> {
                if (error != null || result == null) {
                    tries.getAndIncrement();
                    return;
                }

                limboPlayer.disconnect(target);
                instance.cancelReconnect(uuid);
            });

        }).repeat(VelocityConfig.RECONNECT_DELAY.get(Integer.class), TimeUnit.SECONDS).schedule();

    }

    private void sendTitle(Player player) {
        AtomicInteger dots = new AtomicInteger(0);

        titleTask = instance.getServer().getScheduler().buildTask(instance, () -> {

            if (dots.getAndIncrement() == 4) {
                dots.set(0);
            }

            TitleUtil.sendReconnectingTitle(0, 1 + 20, dots.get(), VelocityMessages.RECONNECT_TITLE, VelocityMessages.RECONNECT_SUB_TITLE, player);
        }).repeat(1, TimeUnit.SECONDS).schedule();
    }

    private void handleFallback(LimboPlayer limboPlayer) {
        List<RegisteredServer> lobbies = Lists.newArrayList(fallingServerManager.getAll());

        if (lobbies.size() == 0) {
            limboPlayer.getProxyPlayer().disconnect(Component.text(ChatUtil.getFormattedString(VelocityMessages.NO_SERVER, new Placeholder("prefix", VelocityMessages.PREFIX.get(String.class)))));
            instance.cancelReconnect(uuid);
            return;
        }

        lobbies.sort(Comparator.comparingInt(o -> o.getPlayersConnected().size()));

        RegisteredServer registeredServer = lobbies.get(0);

        limboPlayer.disconnect(registeredServer);
        instance.cancelReconnect(uuid);
    }

    public void clear() {
        player.showTitle(Title.title(Component.text(" "), Component.text(" ")));
        player.clearTitle();
        player.resetTitle();
    }

}
