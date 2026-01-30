package me.candiesjar.fallbackserver.reconnect.task;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.reconnect.ReconnectManager;
import me.candiesjar.fallbackserver.reconnect.api.ReconnectQueue;
import me.candiesjar.fallbackserver.reconnect.server.ReconnectSession;
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
    private int tries = 0;

    private ScheduledTask task;

    public ReconnectWorker(FallbackServerBungee fallbackServerBungee, ReconnectQueue reconnectQueue) {
        this.fallbackServerBungee = fallbackServerBungee;
        this.proxyServer = fallbackServerBungee.getProxy();
        this.reconnectQueue = reconnectQueue;
        this.reconnectManager = fallbackServerBungee.getReconnectManager();
    }

    public void start() {
        int delay = BungeeConfig.RECONNECT_DELAY.getInt();
        int period = BungeeConfig.RECONNECT_TASK_DELAY.getInt();

        task = proxyServer.getScheduler().schedule(fallbackServerBungee, this::ping, delay, period, TimeUnit.SECONDS);
    }

    private void ping() {
        if (reconnectQueue.getPlayerQueue().isEmpty()) {
            reconnectManager.stopWorker(reconnectQueue.getTarget().getName());
            stop();
            return;
        }

        reconnectQueue.getTarget().ping((result, error) -> {
            if (error != null || !isServerUsable(result)) {
                tries++;

                if (tries >= maxTries) {
                    handleMaxTries();
                    stop();
                }

                return;
            }

        });
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

        int online = result.getPlayers().getOnline();
        int max = result.getPlayers().getMax();
        int check = BungeeConfig.RECONNECT_PLAYER_COUNT_CHECK.getInt();

        if (online >= max) {
            return false;
        }

        return max == check;
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }

        reconnectManager.stopWorker(reconnectQueue.getTarget().getName());
    }
}
