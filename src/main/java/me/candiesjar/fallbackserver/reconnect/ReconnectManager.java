package me.candiesjar.fallbackserver.reconnect;

import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.reconnect.api.ReconnectQueue;
import me.candiesjar.fallbackserver.reconnect.task.ReconnectWorker;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;

public class ReconnectManager {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();

    private final HashMap<String, ReconnectQueue> queueMap = Maps.newHashMap();
    private final HashMap<String, ReconnectWorker> workerMap = Maps.newHashMap();

    public void onKick(ProxiedPlayer player, ServerInfo serverInfo) {
        String serverName = serverInfo.getName();
        ReconnectQueue queue = queueMap.get(serverName);

        if (queue == null) {
            queue = new ReconnectQueue(serverInfo);
            queueMap.put(serverInfo.getName(), queue);
        }

        // TODO: Fixup with proper classes

        // queue.addPlayer(player.getUniqueId());

        if (!workerMap.containsKey(serverName)) {
            ReconnectWorker worker = new ReconnectWorker(fallbackServerBungee, queue);
            worker.start();
            workerMap.put(serverInfo.getName(), worker);
        }
    }
}
