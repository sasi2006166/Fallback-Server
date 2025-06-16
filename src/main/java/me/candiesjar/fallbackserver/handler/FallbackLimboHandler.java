package me.candiesjar.fallbackserver.handler;

import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.TitleMode;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.config.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.ReconnectUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class FallbackLimboHandler implements LimboSessionHandler {

    private final AtomicInteger tries = new AtomicInteger(0);
    private final AtomicInteger dots = new AtomicInteger(0);
    private final int timeout = VelocityConfig.RECONNECT_PING_TIMEOUT.get(Integer.class);
    private final int maxTries = VelocityConfig.RECONNECT_MAX_TRIES.get(Integer.class);
    private final PingOptions.Builder pingOptions = PingOptions.builder().timeout(Duration.ofSeconds(timeout));

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();
    private final Scheduler scheduler = fallbackServerVelocity.getServer().getScheduler();

    @Getter
    private final RegisteredServer target;
    private final UUID uuid;
    private final Player player;

    @Getter
    private LimboPlayer limboPlayer;

    @Getter
    private ScheduledTask reconnectTask, titleTask, connectTask;

    @Override
    public void onSpawn(Limbo server, LimboPlayer limboPlayer) {
        this.limboPlayer = limboPlayer;
        limboPlayer.disableFalling();
        TitleMode titleMode = TitleMode.fromString(VelocityConfig.RECONNECT_TITLE_MODE.get(String.class));

        titleTask = scheduleTask(() -> sendTitles(VelocityMessages.RECONNECT_TITLE, VelocityMessages.RECONNECT_SUB_TITLE),
                getTitleDelay(player), titleMode.getPeriod());

        reconnectTask = scheduleTask(() -> startReconnect(limboPlayer),
                0, VelocityConfig.RECONNECT_TASK_DELAY.get(Integer.class));

        boolean physical = VelocityConfig.RECONNECT_USE_PHYSICAL.get(Boolean.class);
        if (physical) {
            String physicalServerName = VelocityConfig.RECONNECT_PHYSICAL_SERVER.get(String.class);
            RegisteredServer physicalServer = fallbackServerVelocity.getServer().getServer(physicalServerName).orElse(null);
            limboPlayer.disconnect(physicalServer);
        }
    }

    @Override
    public void onChat(String chat) {
        if (chat.startsWith("/")) {
            String command = chat.substring(1);
            fallbackServerVelocity.getServer().getCommandManager().executeImmediatelyAsync(player, command);
            return;
        }

        for (UUID uuid : fallbackServerVelocity.getPlayerCacheManager().getReconnectMap().keySet()) {
            Player target = fallbackServerVelocity.getServer().getPlayer(uuid).orElse(null);

            if (target == null) {
                continue;
            }

            String fullMessage = player.getUsername() + ": " + chat;
            target.sendMessage(Component.text(fullMessage));
        }
    }

    private void startReconnect(LimboPlayer limboPlayer) {
        boolean maxTries = tries.getAndIncrement() == this.maxTries;

        if (maxTries) {
            boolean fallback = VelocityConfig.RECONNECT_USE_FALLBACK.get(Boolean.class);

            if (fallback) {
                handleFallback(limboPlayer);
                return;
            }

            ReconnectUtil.cancelReconnect(uuid);

            boolean physical = VelocityConfig.RECONNECT_USE_PHYSICAL.get(Boolean.class);
            if (physical) {
                player.disconnect(Component.empty());
                return;
            }

            limboPlayer.disconnect();
            return;
        }

        target.ping(pingOptions.build()).whenComplete((ping, throwable) -> {
            if (throwable != null || ping == null) {
                return;
            }

            int connectedPlayers = ping.asBuilder().getOnlinePlayers();
            int maxPlayers = ping.asBuilder().getMaximumPlayers();
            int check = VelocityConfig.RECONNECT_PLAYER_COUNT_CHECK.get(Integer.class);

            if (connectedPlayers == maxPlayers) {
                tries.set(this.maxTries);
                return;
            }

            if (maxPlayers != check) {
                return;
            }

            titleTask.cancel();
            reconnectTask.cancel();

            titleTask = scheduleTask(() -> sendTitles(VelocityMessages.CONNECTING_TITLE, VelocityMessages.CONNECTING_SUB_TITLE), 1, 1);
            connectTask = scheduleTask(() -> {
                target.ping().whenComplete((ping1, throwable1) -> {
                    if (throwable1 != null || ping1 == null) {
                        handleFallback(limboPlayer);
                    }
                });
                handleConnection(limboPlayer);
            }, VelocityConfig.RECONNECT_TASK_DELAY.get(Integer.class) + 2, 0);
        });
    }

    private void handleConnection(LimboPlayer limboPlayer) {
        titleTask.cancel();
        clearPlayerTitle();

        showConnectedTitle();

        boolean clearChat = VelocityConfig.CLEAR_CHAT_RECONNECT.get(Boolean.class);

        if (clearChat) {
            ChatUtil.clearChat(player);
        }

        boolean physical = VelocityConfig.RECONNECT_USE_PHYSICAL.get(Boolean.class);
        if (physical) {
            player.createConnectionRequest(target).fireAndForget();
            return;
        }

        limboPlayer.disconnect(target);
    }

    private void handleFallback(LimboPlayer limboPlayer) {
        limboPlayer.flushPackets();

        /*
            This server always exist and has been loaded by the plugin
            on the startup, it's a fake empty server used for
            bypassing the @NotNull reason provided by Velocity API.
        */

        RegisteredServer registeredServer = fallbackServerVelocity.getServer().getServer("FallbackLimbo").get();
        Component reason = Component.text("Lost Connection");
        KickedFromServerEvent.ServerKickResult serverKickResult = KickedFromServerEvent.RedirectPlayer.create(registeredServer);
        KickedFromServerEvent kickedFromServerEvent = new KickedFromServerEvent(player, registeredServer, reason, false, serverKickResult);
        fallbackServerVelocity.getServer().getEventManager().fireAndForget(kickedFromServerEvent);

        if (kickedFromServerEvent.getResult() instanceof KickedFromServerEvent.RedirectPlayer redirectPlayer) {
            registeredServer = redirectPlayer.getServer();
        }

        boolean physical = VelocityConfig.RECONNECT_USE_PHYSICAL.get(Boolean.class);
        if (kickedFromServerEvent.getResult() instanceof KickedFromServerEvent.DisconnectPlayer) {
            if (physical) {
                player.disconnect(Component.empty());
                return;
            }
            limboPlayer.disconnect();
            return;
        }

        if (physical) {
            clearPlayerTitle();
            player.createConnectionRequest(registeredServer).fireAndForget();
            scheduleTask(() -> {
                ReconnectUtil.cancelReconnect(uuid);
                VelocityMessages.RECONNECTION_FAILED.send(player,
                        new Placeholder("server", target.getServerInfo().getName()),
                        new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            }, 4, 0);
            return;
        }

        clearPlayerTitle();
        limboPlayer.disconnect(registeredServer);
        scheduleTask(() -> {
            ReconnectUtil.cancelReconnect(uuid);
            VelocityMessages.RECONNECTION_FAILED.send(player,
                    new Placeholder("server", target.getServerInfo().getName()),
                    new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
        }, 4, 0);
    }

    private void sendTitles(VelocityMessages title, VelocityMessages subTitle) {
        int currentDots = dots.incrementAndGet() % 5;
        TitleMode titleMode = TitleMode.fromString(VelocityConfig.RECONNECT_TITLE_MODE.get(String.class));

        switch (titleMode) {
            case NORMAL:
                TitleUtil.sendReconnectingTitle(0, 20, currentDots, title, subTitle, player);
                break;
            case STATIC:
                TitleUtil.sendTitle(0, 20, 0, title.get(String.class), subTitle.get(String.class), target, player);
                break;
            case PULSE:
                TitleUtil.sendTitle(1, 20, VelocityMessages.RECONNECT_TITLE_BEAT.get(Integer.class),
                        title.get(String.class), subTitle.get(String.class), target, player);
                break;
        }
    }

    private void showConnectedTitle() {
        int fadeIn = VelocityMessages.CONNECTED_FADE_IN.get(Integer.class);
        int stay = VelocityMessages.CONNECTED_STAY.get(Integer.class);
        int fadeOut = VelocityMessages.CONNECTED_FADE_OUT.get(Integer.class);
        int delay = VelocityMessages.CONNECTED_DELAY.get(Integer.class);

        scheduler.buildTask(fallbackServerVelocity,
                        () -> TitleUtil.sendTitle(fadeIn, stay, fadeOut,
                                VelocityMessages.CONNECTED_TITLE.get(String.class),
                                VelocityMessages.CONNECTED_SUB_TITLE.get(String.class), target, player))
                .delay(delay, TimeUnit.SECONDS)
                .schedule();

        scheduler.buildTask(fallbackServerVelocity, () -> ReconnectUtil.cancelReconnect(uuid))
                .delay(3, TimeUnit.SECONDS)
                .schedule();
    }

    private ScheduledTask scheduleTask(Runnable runnable, int delay, int period) {
        return scheduler.buildTask(fallbackServerVelocity, runnable)
                .delay(delay, TimeUnit.SECONDS)
                .repeat(period, TimeUnit.SECONDS)
                .schedule();
    }

    public void clearPlayerTitle() {
        player.showTitle(Title.title(Component.empty(), Component.empty()));
        player.clearTitle();
        player.resetTitle();
    }

    private int getTitleDelay(Player player) {
        if (player.getProtocolVersion().greaterThan(ProtocolVersion.MINECRAFT_1_20_2)) {
            return 3;
        }
        return 0;
    }
}
