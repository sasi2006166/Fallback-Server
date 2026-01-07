package me.candiesjar.fallbackserver.connection;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
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

import java.util.List;

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

        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("[DOWNSTREAMBRIDGE] Disconnected from server: " + server.getInfo().getName(), false);
        }

        if (proxyServer.getReconnectHandler() != null) {
            proxyServer.getReconnectHandler().setServer(userConnection);
        }

        if (server.isObsolete()) {
            return;
        }

        server.setObsolete(true);

        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("[DOWNSTREAMBRIDGE] Now obsolete: " + server.getInfo().getName(), false);
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
        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("[DOWNSTREAMBRIDGE] Exception on server: " + server.getInfo().getName(), false);
        }

        if (server.isObsolete()) {
            return;
        }

        server.setObsolete(true);

        String reason = proxyServer.getTranslation("lost_connection");
        ServerInfo nextServer = userConnection.updateAndGetNextServer(server.getInfo());
        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, server.getInfo(), TextComponent.fromLegacy(reason), nextServer, ServerKickEvent.State.CONNECTED));

        if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
            userConnection.connect(serverKickEvent.getCancelServer());
        }
    }

    @Override
    public void handle(LoginSuccess loginSuccess) throws Exception {
        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("[DOWNSTREAMBRIDGE] Login success on server: " + server.getInfo().getName(), false);
        }
        super.handle(loginSuccess);
    }

    @Override
    public void handle(Kick kick) {
        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("[DOWNSTREAMBRIDGE] Kicking player from server: " + server.getInfo().getName() + " for reason: " + kick.getMessage().toLegacyText(), false);
        }

        List<String> ignoredReasons = BungeeConfig.IGNORED_REASONS.getStringList();

        if (shouldIgnore(kick.getMessage().toLegacyText(), BungeeConfig.IGNORED_REASONS.getStringList())) {
            userConnection.disconnect(kick.getMessage());
            server.setObsolete(true);
            ErrorHandler.add(Severity.INFO, "[DOWNSTREAMBRIDGE] Kicking player " + userConnection.getName() + " from server: " + server.getInfo().getName() + " for ignored reason: " + kick.getMessage().toLegacyText());
            throw CancelSendSignal.INSTANCE;
        }

        ServerInfo nextServer = userConnection.updateAndGetNextServer(server.getInfo());
        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, server.getInfo(), kick.getMessage(), nextServer, ServerKickEvent.State.CONNECTED));

        if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
            userConnection.connect(serverKickEvent.getCancelServer());
        }

        server.setObsolete(true);
        throw CancelSendSignal.INSTANCE;
    }

    private boolean shouldIgnore(String reason, List<String> ignoredReasons) {
        if (reason == null || ignoredReasons == null) {
            return false;
        }

        for (String word : ignoredReasons) {

            if (reason.contains(word)) {
                return true;
            }

        }
        return false;
    }
}