package me.candiesjar.fallbackserver.reconnect.api;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.candiesjar.fallbackserver.reconnect.server.ReconnectSession;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Queue;

public class ReconnectQueue {

    @Getter
    private final ServerInfo target;
    @Getter
    private final Queue<ReconnectSession> playerQueue = Lists.newLinkedList();

    public ReconnectQueue(ServerInfo target) {
        this.target = target;
    }

    public void addSession(ReconnectSession reconnectSession) {
        if (!playerQueue.contains(reconnectSession)) {
            playerQueue.add(reconnectSession);
        }
    }

    public ReconnectSession pollPlayer() {
        return playerQueue.poll();
    }

    // For future updates.
    public int getSize() {
        return playerQueue.size();
    }

}
