package me.candiesjar.fallbackserver.reconnect;

import com.google.common.collect.Maps;
import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.reconnect.api.ReconnectQueue;
import me.candiesjar.fallbackserver.reconnect.server.ReconnectSession;
import me.candiesjar.fallbackserver.reconnect.task.ReconnectWorker;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Map;

public class ReconnectManager {

    private final FallbackServerBungee fallbackServerBungee;

    @Getter
    private final Map<String, ReconnectQueue> queueMap = Maps.newConcurrentMap();
    @Getter
    private final Map<String, ReconnectWorker> workerMap = Maps.newConcurrentMap();

    public ReconnectManager(FallbackServerBungee fallbackServerBungee) {
        this.fallbackServerBungee = fallbackServerBungee;
    }

    public void onKick(ReconnectSession reconnectSession, ServerInfo serverInfo) {
        String serverName = serverInfo.getName();
        ReconnectQueue queue = queueMap.computeIfAbsent(serverName, k -> new ReconnectQueue(serverInfo));

        queue.addSession(reconnectSession);

        workerMap.computeIfAbsent(serverName, k -> {
            ReconnectWorker worker = new ReconnectWorker(fallbackServerBungee, queue);
            worker.start();
            return worker;
        });
    }

    public void removeSession(ReconnectSession session) {
        String name = session.getTargetServerInfo().getName();

        queueMap.computeIfPresent(name, (serverName, queue) -> {
            queue.removeSession(session);

            if (queue.getPlayerQueue().isEmpty()) {
                return null;
            }

            return queue;
        });
    }

    public void stopWorker(String serverName) {
        ReconnectWorker worker = workerMap.remove(serverName);
        if (worker != null) {
            worker.stop();
        }
        queueMap.remove(serverName);
    }
}
