package me.candiesjar.fallbackserver.handler;

import com.google.common.collect.Lists;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.server.impl.FallingServerManager;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.ServerUtils;
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

    private final AtomicInteger TRIES = new AtomicInteger(0);
    private final AtomicInteger DOTS = new AtomicInteger(0);
    private final int TIMEOUT = VelocityConfig.RECONNECT_PING_TIMEOUT.get(Integer.class);
    private final int MAX_TRIES = VelocityConfig.RECONNECT_MAX_TRIES.get(Integer.class);
    private final PingOptions.Builder PING_OPTIONS = PingOptions.builder().timeout(Duration.ofSeconds(TIMEOUT));

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();
    private final Scheduler scheduler = fallbackServerVelocity.getServer().getScheduler();
    private final FallingServerManager fallingServerManager = fallbackServerVelocity.getFallingServerManager();

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

        titleTask = scheduler.buildTask(fallbackServerVelocity, () -> sendTitle(VelocityMessages.RECONNECT_TITLE, VelocityMessages.RECONNECT_SUB_TITLE)).repeat(1, TimeUnit.SECONDS).schedule();

        int delay = VelocityConfig.RECONNECT_TASK_DELAY.get(Integer.class);

        reconnectTask = scheduler.buildTask(fallbackServerVelocity, () -> start(limboPlayer)).repeat(delay, TimeUnit.SECONDS).schedule();
    }

    private void start(LimboPlayer limboPlayer) {
        boolean maxTries = TRIES.getAndIncrement() == MAX_TRIES;

        if (maxTries) {
            boolean fallback = VelocityConfig.RECONNECT_USE_FALLBACK.get(Boolean.class);

            if (fallback) {
                handleFallback(limboPlayer);
                return;
            }

            fallbackServerVelocity.cancelReconnect(uuid);
            limboPlayer.disconnect();
            return;
        }

        target.ping(PING_OPTIONS.build()).whenComplete((ping, throwable) -> {
            if (throwable != null || ping == null) {
                return;
            }

            int maxPlayers = ping.asBuilder().getMaximumPlayers();
            int check = VelocityConfig.RECONNECT_PLAYER_COUNT_CHECK.get(Integer.class);

            if (maxPlayers == check) {
                killTasks();
                clear();
                resetDots();

                titleTask = scheduler.buildTask(fallbackServerVelocity, () -> sendTitle(VelocityMessages.CONNECTING_TITLE, VelocityMessages.CONNECTING_SUB_TITLE)).repeat(1, TimeUnit.SECONDS).schedule();
                connectTask = scheduler.buildTask(fallbackServerVelocity, () -> handleConnection(limboPlayer)).delay(VelocityConfig.RECONNECT_TASK_DELAY.get(Integer.class), TimeUnit.SECONDS).schedule();
            }

        });

    }

    private void handleConnection(LimboPlayer limboPlayer) {
        limboPlayer.disconnect(target);
        titleTask.cancel();
        clear();

        int fadeIn = VelocityMessages.CONNECTED_FADE_IN.get(Integer.class);
        int stay = VelocityMessages.CONNECTED_STAY.get(Integer.class);
        int fadeOut = VelocityMessages.CONNECTED_FADE_OUT.get(Integer.class);
        int delay = VelocityMessages.CONNECTED_DELAY.get(Integer.class);

        scheduler.buildTask(fallbackServerVelocity, () -> TitleUtil.sendTitle(fadeIn, stay, fadeOut, VelocityMessages.CONNECTED_TITLE.get(String.class), VelocityMessages.CONNECTED_SUB_TITLE.get(String.class), player)).delay(delay, TimeUnit.SECONDS).schedule();

        boolean clearChat = VelocityConfig.CLEAR_CHAT_RECONNECT.get(Boolean.class);

        if (clearChat) {
            ChatUtil.clearChat(player);
        }

        fallbackServerVelocity.cancelReconnect(uuid);
    }

    private void handleFallback(LimboPlayer limboPlayer) {
        List<RegisteredServer> lobbies = Lists.newArrayList(fallingServerManager.getAll());

        boolean hasMaintenance = fallbackServerVelocity.isMaintenance();

        if (hasMaintenance) {
            lobbies.removeIf(ServerUtils::isMaintenance);
        }

        if (lobbies.isEmpty()) {
            player.disconnect(Component.text(ChatUtil.getFormattedString(VelocityMessages.NO_SERVER, new Placeholder("prefix", VelocityMessages.PREFIX.get(String.class)))));
            fallbackServerVelocity.cancelReconnect(uuid);
            return;
        }

        lobbies.sort(Comparator.comparingInt(o -> o.getPlayersConnected().size()));

        RegisteredServer registeredServer = lobbies.get(0);

        limboPlayer.disconnect(registeredServer);
        fallbackServerVelocity.cancelReconnect(uuid);

        boolean clearChat = VelocityConfig.CLEAR_CHAT_FALLBACK.get(Boolean.class);

        if (clearChat) {
            ChatUtil.clearChat(player);
        }

        VelocityMessages.CONNECTION_FAILED.send(player, new Placeholder("prefix", VelocityMessages.PREFIX.get(String.class)));

        boolean useTitle = VelocityMessages.USE_FALLBACK_TITLE.get(Boolean.class);

        if (useTitle) {
            scheduler.buildTask(fallbackServerVelocity, () ->
                            TitleUtil.sendTitle(
                                    VelocityMessages.FALLBACK_FADE_IN.get(Integer.class),
                                    VelocityMessages.FALLBACK_STAY.get(Integer.class),
                                    VelocityMessages.FALLBACK_FADE_OUT.get(Integer.class),
                                    ChatUtil.color(VelocityMessages.FALLBACK_TITLE.get(String.class)),
                                    ChatUtil.color(VelocityMessages.FALLBACK_SUB_TITLE.get(String.class)),
                                    player
                            )).delay(VelocityMessages.FALLBACK_DELAY.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
        }

    }

    private void sendTitle(VelocityMessages title, VelocityMessages subTitle) {
        int currentDots = DOTS.incrementAndGet() % 5;
        TitleUtil.sendReconnectingTitle(0, 1 + 20, currentDots, title, subTitle, player);
    }

    private void resetDots() {
        DOTS.set(0);
    }

    private void killTasks() {
        reconnectTask.cancel();
        titleTask.cancel();
    }

    public void clear() {
        player.showTitle(Title.title(Component.empty(), Component.empty()));
        player.clearTitle();
        player.resetTitle();
    }

}
