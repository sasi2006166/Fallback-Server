package me.candiesjar.fallbackserver.queue;

import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.UserConnection;

public class QueueHandler {

    private final UserConnection userConnection;
    private final BungeeServerInfo target;

    public QueueHandler(UserConnection userConnection, BungeeServerInfo target) {
        this.userConnection = userConnection;
        this.target = target;
    }

}
