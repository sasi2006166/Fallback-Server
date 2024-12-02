package me.candiesjar.fallbackserver.api;

import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

@Getter
public class FallbackEvent extends Event {

    private final ProxiedPlayer proxiedPlayer;
    private final ServerInfo kickedFrom;
    private final ServerInfo kickedTo;
    private final String reason;
    private final boolean isReconnect;

    public FallbackEvent(ProxiedPlayer proxiedPlayer, ServerInfo kickedFrom, ServerInfo kickedTo, String reason, boolean isReconnect) {
        this.proxiedPlayer = proxiedPlayer;
        this.kickedFrom = kickedFrom;
        this.kickedTo = kickedTo;
        this.reason = reason;
        this.isReconnect = isReconnect;
    }

    private void redirectPlayer(ServerInfo serverInfo) {
        proxiedPlayer.connect(serverInfo);
    }

    private void kickPlayer(String reason) {
        proxiedPlayer.disconnect(new TextComponent(reason));
    }

    private void kickPlayer() {
        proxiedPlayer.disconnect();
    }

}
