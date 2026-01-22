package me.candiesjar.fallbackserver.reconnect.task;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.reconnect.api.ReconnectQueue;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class ReconnectWorker {

    private final FallbackServerBungee fallbackServerBungee;
    private final ProxyServer proxyServer;
    private final ReconnectQueue reconnectQueue;

    private ScheduledTask task;

    public ReconnectWorker(FallbackServerBungee fallbackServerBungee, ReconnectQueue reconnectQueue) {
        this.fallbackServerBungee = fallbackServerBungee;
        this.proxyServer = fallbackServerBungee.getProxy();
        this.reconnectQueue = reconnectQueue;
    }

    public void start() {
        int delay = BungeeConfig.RECONNECT_DELAY.getInt();
        int period = BungeeConfig.RECONNECT_TASK_DELAY.getInt();

        task = proxyServer.getScheduler().schedule(fallbackServerBungee, this::ping, delay, period, TimeUnit.SECONDS);
    }

    private void ping() {
        reconnectQueue.getTarget().ping((result, error) -> {
            if (reconnectQueue.getPlayerQueue().isEmpty()) {
                stop();
                return;
            }

            if (error != null || result == null) {
                return;
            }

            int playerCount = result.getPlayers().getOnline();
            int maxPlayers = result.getPlayers().getMax();
            int check = BungeeConfig.RECONNECT_PLAYER_COUNT_CHECK.getInt();

            if (playerCount == maxPlayers) {
                return;
            }

            if (maxPlayers != check) {
                return;
            }

        });
    }

    private void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}
