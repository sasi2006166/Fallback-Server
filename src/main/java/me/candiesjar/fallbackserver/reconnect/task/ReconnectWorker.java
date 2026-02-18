package me.candiesjar.fallbackserver.reconnect.task;

import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
import me.candiesjar.fallbackserver.reconnect.ReconnectManager;
import me.candiesjar.fallbackserver.reconnect.api.ReconnectQueue;
import me.candiesjar.fallbackserver.reconnect.server.ReconnectSession;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class ReconnectWorker {

    private final FallbackServerBungee fallbackServerBungee;
    private final ReconnectManager reconnectManager;
    private final ProxyServer proxyServer;
    private final ReconnectQueue reconnectQueue;

    private final int maxTries = BungeeConfig.RECONNECT_TRIES.getInt();
    private final boolean kick = BungeeConfig.RECONNECT_SORT.getBoolean();

    private int playersToConnect = 2;
    private int tries = 0;

    @Getter
    private ScheduledTask pingTask, connectTask;

    public ReconnectWorker(FallbackServerBungee fallbackServerBungee, ReconnectQueue reconnectQueue) {
        this.fallbackServerBungee = fallbackServerBungee;
        this.proxyServer = fallbackServerBungee.getProxy();
        this.reconnectQueue = reconnectQueue;
        this.reconnectManager = fallbackServerBungee.getReconnectManager();
    }

    public void start() {
        int delay = BungeeConfig.RECONNECT_DELAY.getInt();
        int period = BungeeConfig.RECONNECT_TASK_DELAY.getInt();
        int configured = BungeeConfig.RECONNECT_PLAYERS_PER_TICK.getInt();

        if (configured <= 0) {
            ErrorHandler.add(Severity.ERROR, "[RECONNECT] Invalid players per tick value: " + configured + ". Defaulting to 2.");
        }

        playersToConnect = configured > 0 ? configured : 2;

        if (fallbackServerBungee.isDebug()) {
            Utils.log("[RECONNECT] Starting reconnect worker for server " + reconnectQueue.getTarget().getName(), false);
        }

        pingTask = proxyServer.getScheduler().schedule(fallbackServerBungee, this::ping, delay, period, TimeUnit.SECONDS);
    }

    private void ping() {
        if (reconnectQueue.getPlayerQueue().isEmpty()) {
            ErrorHandler.add(Severity.INFO, "[RECONNECT] No players left in queue for server " + reconnectQueue.getTarget().getName() + ". Stopping worker...");
            reconnectManager.stopWorker(reconnectQueue.getTarget().getName());
            return;
        }

        reconnectQueue.getTarget().ping((result, error) -> {
            if (error != null || !isServerUsable(result)) {
                tries++;

                if (tries >= maxTries) {
                    handleMaxTries();
                    reconnectManager.stopWorker(reconnectQueue.getTarget().getName());
                }

                return;
            }

            tries = 0;
            stopPing();
            startConnect();
        });
    }

    private void startConnect() {
        int period = BungeeConfig.RECONNECT_CONNECTION_DELAY.getInt();
        connectTask = proxyServer.getScheduler().schedule(fallbackServerBungee, this::connectPlayers, 0, period, TimeUnit.SECONDS);
    }

    private void connectPlayers() {
        for (int i = 0; i < playersToConnect; i++) {
            ReconnectSession session = reconnectQueue.pollPlayer();
            if (session == null) break;
            session.handleConnection();
        }

        if (reconnectQueue.getPlayerQueue().isEmpty()) {
            stop();
        }
    }

    private void handleMaxTries() {
        while (!reconnectQueue.getPlayerQueue().isEmpty()) {
            ReconnectSession session = reconnectQueue.pollPlayer();

            if (session == null) {
                continue;
            }

            session.handleFallback(kick);
        }
    }

    private boolean isServerUsable(ServerPing result) {
        if (result == null) {
            return false;
        }

        int max = result.getPlayers().getMax();
        int check = BungeeConfig.RECONNECT_PLAYER_COUNT_CHECK.getInt();

        return max == check;
    }

    private void stopPing() {
        if (pingTask != null) {
            pingTask.cancel();
            pingTask = null;
        }
    }

    private void stopConnect() {
        if (connectTask != null) {
            connectTask.cancel();
            connectTask = null;
        }
    }

    public void stop() {
        stopPing();
        stopConnect();
    }
}
