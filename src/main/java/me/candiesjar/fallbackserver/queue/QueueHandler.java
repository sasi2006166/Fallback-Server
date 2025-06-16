package me.candiesjar.fallbackserver.queue;

import com.google.common.collect.Maps;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.UUID;

public class QueueHandler {

    private final HashMap<UUID, ServerInfo> serverInfoMap = Maps.newHashMap();

    public QueueHandler(ProxiedPlayer player, ServerInfo targetServer) {
        if (serverInfoMap.containsKey(player.getUniqueId())) {
            return;
        }

        addToQueue(player.getUniqueId(), targetServer);
    }

    public void addToQueue(UUID uuid, ServerInfo targetServer) {
        serverInfoMap.put(uuid, targetServer);
    }
}
