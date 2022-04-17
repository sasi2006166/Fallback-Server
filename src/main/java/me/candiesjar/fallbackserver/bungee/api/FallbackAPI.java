package me.candiesjar.fallbackserver.bungee.api;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class FallbackAPI extends Event implements Cancellable {

    private boolean cancelled = false;
    private final ProxiedPlayer player;
    private final ServerInfo kickedFrom;
    private final ServerInfo kickedTo;
    private final String kickedMessage;

    public FallbackAPI(ProxiedPlayer player, ServerInfo kickedFrom, ServerInfo kickedTo, String kickedMessage) {
        this.player = player;
        this.kickedFrom = kickedFrom;
        this.kickedTo = kickedTo;
        this.kickedMessage = kickedMessage;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public ServerInfo getKickedFrom() {
        return kickedFrom;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    public ServerInfo getKickedTo() {
        return kickedTo;
    }

    public String getKickedMessage() {
        return kickedMessage;
    }
}
