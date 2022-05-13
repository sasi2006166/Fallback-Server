package me.candiesjar.fallbackserver.api;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class HubAPI extends Event implements Cancellable {

    private boolean cancelled = false;
    private final ProxiedPlayer player;
    private final ServerInfo playerServer;
    private final ServerInfo connectTo;

    public HubAPI(ProxiedPlayer player, ServerInfo playerServer, ServerInfo connectTo) {
        this.player = player;
        this.playerServer = playerServer;
        this.connectTo = connectTo;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public ServerInfo getConnectTo() {
        return connectTo;
    }

    public ServerInfo getPlayerServer() {
        return playerServer;
    }
}
