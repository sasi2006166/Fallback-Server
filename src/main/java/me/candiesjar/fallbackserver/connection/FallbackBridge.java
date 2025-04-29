package me.candiesjar.fallbackserver.connection;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LoginSuccess;

public class FallbackBridge extends DownstreamBridge {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();

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

        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("[DOWNSTREAMBRIDGE] Disconnected from server: " + server.getInfo().getName(), true);
        }

        ServerInfo nextServer = userConnection.updateAndGetNextServer(server.getInfo());
        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, server.getInfo(), TextComponent.fromLegacy("Crashed"), nextServer, ServerKickEvent.State.CONNECTED));

        if (userConnection.isConnected()) {
            if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
                userConnection.connect(serverKickEvent.getCancelServer());
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

        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("[DOWNSTREAMBRIDGE] Exception on server: " + server.getInfo().getName(), true);
        }

        ServerInfo nextServer = userConnection.updateAndGetNextServer(server.getInfo());
        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, server.getInfo(), TextComponent.fromLegacy(reason), nextServer, ServerKickEvent.State.CONNECTED));

        if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
            userConnection.connect(serverKickEvent.getCancelServer());
        }
    }

    @Override
    public void handle(LoginSuccess loginSuccess) throws Exception {
        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("[DOWNSTREAMBRIDGE] Login success on server: " + server.getInfo().getName(), true);
        }
        super.handle(loginSuccess);
    }

    @Override
    public void handle(Kick kick) {
        ServerInfo nextServer = userConnection.updateAndGetNextServer(server.getInfo());
        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, server.getInfo(), kick.getMessage(), nextServer, ServerKickEvent.State.CONNECTED));

        if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
            userConnection.connect(serverKickEvent.getCancelServer());
        }

        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("[DOWNSTREAMBRIDGE] Kick from server: " + server.getInfo().getName(), true);
        }

        server.setObsolete(true);
        throw CancelSendSignal.INSTANCE;
    }
}