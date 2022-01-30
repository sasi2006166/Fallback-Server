package me.candiesjar.fallbackserver.bungee.api;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

public class FallbackAPI extends Event implements Cancellable {

    private final ProxiedPlayer player;
    private final ServerInfo kickedFrom;
    private static FallbackServerBungee fallbackServerBungee;

    public FallbackAPI(ProxiedPlayer player, ServerInfo kickedFrom) {
        this.player = player;
        this.kickedFrom = kickedFrom;
    }

    public static FallbackServerBungee getFallbackServer() {
        return fallbackServerBungee;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public ServerInfo getKickedFrom() {
        return kickedFrom;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }
}
