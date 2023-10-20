package me.candiesjar.fallbackserver.connection;

import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.netty.ChannelWrapper;

public class FallbackBridge extends DownstreamBridge {

    private final UserConnection userConnection;
    private final ProxyServer proxyServer;
    private final ServerConnection server;

    public FallbackBridge(ProxyServer bungee, UserConnection con, ServerConnection server) {
        super(bungee, con, server);
        this.userConnection = con;
        this.proxyServer = ProxyServer.getInstance();
        this.server = server;
    }

    @Override
    public void disconnected(ChannelWrapper channel) {
        server.getInfo().removePlayer(userConnection);

        if (proxyServer.getReconnectHandler() != null) {
            proxyServer.getReconnectHandler().setServer(userConnection);
        }

        if (server.isObsolete()) {
            return;
        }

        server.setObsolete(true);

        ServerInfo nextServer = userConnection.updateAndGetNextServer(server.getInfo());
        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, server.getInfo(), TextComponent.fromLegacyText("crash"), nextServer, ServerKickEvent.State.CONNECTED));

        if (userConnection.isConnected()) {
            if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
            }
        }

        ServerDisconnectEvent serverDisconnectEvent = new ServerDisconnectEvent(userConnection, server.getInfo());
        proxyServer.getPluginManager().callEvent(serverDisconnectEvent);

    }

    @Override
    public void exception(Throwable t) {
        String reason = proxyServer.getTranslation("lost_connection");

        if (server.isObsolete()) {
            return;
        }

        server.setObsolete(true);

        ServerInfo nextServer = userConnection.updateAndGetNextServer(server.getInfo());
        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, server.getInfo(), TextComponent.fromLegacyText(reason), nextServer, ServerKickEvent.State.CONNECTED));

        if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
        }

    }
}