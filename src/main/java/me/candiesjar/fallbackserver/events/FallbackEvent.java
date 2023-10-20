package me.candiesjar.fallbackserver.events;

import lombok.Getter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class FallbackEvent extends Event {

    @Getter
    private final ProxiedPlayer player;

    @Getter
    private final ServerInfo kickServer;

    @Getter
    private final String reason;

    public FallbackEvent(ProxiedPlayer player, ServerInfo kickServer, String reason) {
        this.player = player;
        this.kickServer = kickServer;
        this.reason = reason;
    }

}
