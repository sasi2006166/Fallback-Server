package me.candiesjar.fallbackserver.reconnect.api;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Queue;
import java.util.UUID;

public class ReconnectQueue {

    @Getter
    private final ServerInfo target;
    @Getter
    private final Queue<UUID> playerQueue = Lists.newLinkedList();

    public ReconnectQueue(ServerInfo target) {
        this.target = target;
    }

    public void addPlayer(UUID playerUUID) {
        if (!playerQueue.contains(playerUUID)) {
            playerQueue.add(playerUUID);
        }
    }

    public UUID pollPlayer() {
        return playerQueue.poll();
    }

    // For future updates.
    public int getSize() {
        return playerQueue.size();
    }

}
