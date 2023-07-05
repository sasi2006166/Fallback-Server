package me.candiesjar.fallbackserver.connection;

import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.*;

public class ReconnectBridge extends DownstreamBridge {

    private final DownstreamBridge previous;
    private final UserConnection userConnection;
    private final ServerConnection server;
    private final ProxyServer proxyServer;

    public ReconnectBridge(ProxyServer bungee, UserConnection userConnection, ServerConnection server, DownstreamBridge previous) {
        super(bungee, userConnection, server);
        this.previous = previous;
        this.userConnection = userConnection;
        this.server = server;
        this.proxyServer = ProxyServer.getInstance();
    }

    @Override
    public void disconnected(ChannelWrapper channel) {

        String reason = proxyServer.getTranslation("lost_connection");

        server.getInfo().removePlayer(userConnection);

        if (proxyServer.getReconnectHandler() != null) {
            proxyServer.getReconnectHandler().setServer(userConnection);
        }

        if (server.isObsolete()) {
            return;
        }

        server.setObsolete(true);

        if (userConnection.isConnected()) {
            ServerInfo nextServer = userConnection.updateAndGetNextServer(server.getInfo());
            ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, server.getInfo(), TextComponent.fromLegacyText(reason), nextServer, ServerKickEvent.State.CONNECTED, ServerKickEvent.Cause.LOST_CONNECTION));

            if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
                userConnection.connectNow(serverKickEvent.getCancelServer(), ServerConnectEvent.Reason.KICK_REDIRECT);
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
        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, server.getInfo(), TextComponent.fromLegacyText(reason), nextServer, ServerKickEvent.State.CONNECTED, ServerKickEvent.Cause.LOST_CONNECTION));

        if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
            userConnection.connectNow(serverKickEvent.getCancelServer(), ServerConnectEvent.Reason.KICK_REDIRECT);
        }

    }

    @Override
    public void handle(Kick kick) {

        ServerInfo nextServer = userConnection.updateAndGetNextServer(server.getInfo());
        ServerKickEvent serverKickEvent = proxyServer.getPluginManager().callEvent(new ServerKickEvent(userConnection, server.getInfo(), ComponentSerializer.parse(kick.getMessage()), nextServer, ServerKickEvent.State.CONNECTED, ServerKickEvent.Cause.EXCEPTION));

        if (serverKickEvent.isCancelled() && serverKickEvent.getCancelServer() != null) {
            userConnection.connectNow(serverKickEvent.getCancelServer(), ServerConnectEvent.Reason.KICK_REDIRECT);
        }

        server.setObsolete(true);

        throw CancelSendSignal.INSTANCE;
    }

    @Override
    public boolean shouldHandle(PacketWrapper packet) throws Exception {
        return previous.shouldHandle(packet);
    }

    @Override
    public void handle(SetCompression setCompression) throws Exception {
        previous.handle(setCompression);
    }

    @Override
    public void handle(PluginMessage pluginMessage) throws Exception {
        previous.handle(pluginMessage);
    }

    @Override
    public void handle(ScoreboardObjective objective) throws Exception {
        previous.handle(objective);
    }

    @Override
    public void handle(EntityEffect entityEffect) throws Exception {
        previous.handle(entityEffect);
    }

    @Override
    public void handle(ServerData serverData) throws Exception {
        previous.handle(serverData);
    }

    @Override
    public void handle(PlayerListItem playerList) throws Exception {
        previous.handle(playerList);
    }

    @Override
    public void handle(ScoreboardScore score) throws Exception {
        previous.handle(score);
    }

    @Override
    public void handle(PacketWrapper packet) throws Exception {
        previous.handle(packet);
    }

    @Override
    public void handle(Commands commands) throws Exception {
        previous.handle(commands);
    }

    @Override
    public void handle(Respawn respawn) {
        previous.handle(respawn);
    }

    @Override
    public void handle(KeepAlive alive) throws Exception {
        previous.handle(alive);
    }

    @Override
    public void handle(BossBar bossBar) {
        previous.handle(bossBar);
    }

    @Override
    public void handle(Team team) throws Exception {
        previous.handle(team);
    }

    @Override
    public void handle(Chat chat) throws Exception {
        previous.handle(chat);
    }

    @Override
    public void handle(Login login) throws Exception {
        previous.handle(login);
    }

    @Override
    public void handle(Title title) throws Exception {
        previous.handle(title);
    }

    @Override
    public void handle(Subtitle title) throws Exception {
        previous.handle(title);
    }

    @Override
    public void handle(ClientChat chat) throws Exception {
        previous.handle(chat);
    }

    @Override
    public void handle(LegacyPing ping) throws Exception {
        previous.handle(ping);
    }

    @Override
    public void handle(PingPacket ping) throws Exception {
        previous.handle(ping);
    }

    @Override
    public void handle(SystemChat chat) throws Exception {
        previous.handle(chat);
    }

    @Override
    public void handle(TitleTimes title) throws Exception {
        previous.handle(title);
    }

    @Override
    public void handle(ClearTitles title) throws Exception {
        previous.handle(title);
    }

    @Override
    public void handle(EntityStatus status) throws Exception {
        previous.handle(status);
    }

    @Override
    public void handle(GameState gameState) throws Exception {
        previous.handle(gameState);
    }

    @Override
    public void handle(Handshake handshake) throws Exception {
        previous.handle(handshake);
    }

    @Override
    public void handle(ClientCommand command) throws Exception {
        previous.handle(command);
    }

    @Override
    public void handle(ClientSettings settings) throws Exception {
        previous.handle(settings);
    }

    @Override
    public void handle(ClientStatus clientStatus) throws Exception {
        previous.handle(clientStatus);
    }

    @Override
    public void handle(LoginRequest loginRequest) throws Exception {
        previous.handle(loginRequest);
    }

    @Override
    public void handle(LoginSuccess loginSuccess) throws Exception {
        previous.handle(loginSuccess);
    }

    @Override
    public void handle(ViewDistance viewDistance) throws Exception {
        previous.handle(viewDistance);
    }

    @Override
    public void handle(LoginPayloadRequest request) throws Exception {
        previous.handle(request);
    }

    @Override
    public void handle(StatusRequest statusRequest) throws Exception {
        previous.handle(statusRequest);
    }

    @Override
    public void handle(LoginPayloadResponse response) throws Exception {
        previous.handle(response);
    }

    @Override
    public void handle(StatusResponse statusResponse) throws Exception {
        previous.handle(statusResponse);
    }

    @Override
    public void handle(TabCompleteRequest tabComplete) throws Exception {
        previous.handle(tabComplete);
    }

    @Override
    public void handle(EntityRemoveEffect removeEffect) throws Exception {
        previous.handle(removeEffect);
    }

    @Override
    public void handle(LegacyHandshake legacyHandshake) throws Exception {
        previous.handle(legacyHandshake);
    }

    @Override
    public void handle(PlayerListItemRemove playerList) throws Exception {
        previous.handle(playerList);
    }

    @Override
    public void handle(PlayerListItemUpdate playerList) throws Exception {
        previous.handle(playerList);
    }

    @Override
    public void handle(EncryptionRequest encryptionRequest) throws Exception {
        previous.handle(encryptionRequest);
    }

    @Override
    public void handle(ScoreboardDisplay displayScoreboard) throws Exception {
        previous.handle(displayScoreboard);
    }

    @Override
    public void handle(EncryptionResponse encryptionResponse) throws Exception {
        previous.handle(encryptionResponse);
    }

    @Override
    public void handle(TabCompleteResponse tabCompleteResponse) throws Exception {
        previous.handle(tabCompleteResponse);
    }

    @Override
    public void handle(PlayerListHeaderFooter playerListHeaderFooter) throws Exception {
        previous.handle(playerListHeaderFooter);
    }

}