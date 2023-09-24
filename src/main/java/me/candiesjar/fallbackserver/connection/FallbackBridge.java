package me.candiesjar.fallbackserver.connection;

import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.chat.ComponentSerializer;
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

        Utils.printDebug("FallbackBridge#disconnected", false);

        server.getInfo().removePlayer(userConnection);

        if (proxyServer.getReconnectHandler() != null) {
            proxyServer.getReconnectHandler().setServer(userConnection);
        }

        if (server.isObsolete()) {
            return;
        }

        server.setObsolete(true);

        ServerInfo nextServer = userConnection.updateAndGetNextServer(server.getInfo());
        ServerKickEvent serverKickEvent = new ServerKickEvent(userConnection, server.getInfo(), ComponentSerializer.parse("crash"), nextServer, ServerKickEvent.State.CONNECTED);

        if (userConnection.isConnected()) {

            if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
                userConnection.connectNow(nextServer, ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT);
            } else {
                proxyServer.getPluginManager().callEvent(serverKickEvent);
            }

        }

        ServerDisconnectEvent serverDisconnectEvent = new ServerDisconnectEvent(userConnection, server.getInfo());
        proxyServer.getPluginManager().callEvent(serverDisconnectEvent);

    }

}