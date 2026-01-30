package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.objects.ServerType;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.reconnect.ReconnectManager;
import me.candiesjar.fallbackserver.reconnect.server.ReconnectSession;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class ServerKickListener implements Listener {

    private final FallbackServerBungee plugin;
    private final ReconnectManager reconnectManager;
    private final ChatUtil chatUtil;
    private final PlayerCacheManager playerCacheManager;
    private final ServerTypeManager serverTypeManager;
    private final OnlineLobbiesManager onlineLobbiesManager;
    private final Map<String, LongAdder> pendingConnections;

    public ServerKickListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
        this.chatUtil = plugin.getChatUtil();
        this.serverTypeManager = plugin.getServerTypeManager();
        this.onlineLobbiesManager = plugin.getOnlineLobbiesManager();
        this.reconnectManager = plugin.getReconnectManager();
        this.playerCacheManager = plugin.getPlayerCacheManager();
        this.pendingConnections = Maps.newConcurrentMap();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKick(ServerKickEvent event) {
        if (!shouldHandle(event)) {
            return;
        }

        ProxiedPlayer player = event.getPlayer();
        ServerInfo kickedFrom = event.getKickedFrom();
        String kickedName = kickedFrom == null ? "ReconnectLimbo" : kickedFrom.getName();
        String group = Optional.ofNullable(ServerManager.getGroupByServer(kickedName)).orElse("default");
        boolean isEmpty = event.getReason() == null;
        String reason = isEmpty ? "Lost Connection" : BaseComponent.toLegacyText(event.getReason()).trim();
        ServerType serverType = serverTypeManager.get(group);

        if (plugin.isDebug()) {
            Utils.printDebug("Player " + player.getName() + " was kicked from " + kickedName, false);
            Utils.printDebug("Reason: " + reason, false);
            Utils.printDebug("Player's group: " + group, false);
            Utils.printDebug("Server type: " + serverType, false);
        }

        ErrorHandler.add(Severity.WARNING, "[KICK] Server " + kickedName + " kicked player " + player.getName() + " for reason: " + reason + " | Group: " + group);

        if (serverType == null || kickedName.equalsIgnoreCase("ReconnectLimbo")) {
            handleFallback(event, kickedFrom, player, reason, kickedName, group);
            return;
        }

        boolean isReconnect = serverType.isReconnect();

        if (isReconnect) {
            handleReconnect(event, reason, kickedName, player);
            return;
        }

        handleFallback(event, kickedFrom, player, reason, kickedName, group);
    }

    private void handleFallback(ServerKickEvent event, ServerInfo kickedFrom, ProxiedPlayer player, String reason, String serverName, String group) {
        boolean ignoredServer = BungeeConfig.USE_IGNORED_SERVERS.getBoolean() && BungeeConfig.IGNORED_SERVER_LIST.getStringList().contains(serverName);

        if (ignoredServer) {
            return;
        }

        List<ServerInfo> lobbies = Lists.newArrayList(onlineLobbiesManager.get(group));

        lobbies.removeIf(Objects::isNull);
        lobbies.remove(kickedFrom);

        if (lobbies.isEmpty()) {
            ErrorHandler.add(Severity.ERROR, "[FALLBACK] No lobbies for player " + player.getName() + " in group " + group);
            if (reason.isEmpty()) {
                player.disconnect(new TextComponent(chatUtil.getFormattedString(BungeeMessages.NO_SERVER)));
                return;
            }
            player.disconnect(new TextComponent(reason));
            return;
        }

        event.setCancelled(true);

        lobbies.sort(Comparator.comparingInt(server -> server.getPlayers().size() + getPendingConnections(server.getName())));
        ServerInfo serverInfo = lobbies.get(0);

        event.setCancelServer(serverInfo);
        incrementPendingConnections(serverInfo.getName());

        plugin.getProxy().getScheduler().schedule(plugin, () -> decrementPendingConnections(serverInfo.getName()), 1, TimeUnit.SECONDS);

        boolean clearChat = BungeeConfig.CLEAR_CHAT_FALLBACK.getBoolean();

        if (clearChat) {
            chatUtil.clearChat(player);
        }

        boolean useTitle = BungeeMessages.USE_FALLBACK_TITLE.getBoolean();

        if (useTitle) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.getTitleUtil().sendTitle(BungeeMessages.FALLBACK_FADE_IN.getInt(),
                            BungeeMessages.FALLBACK_STAY.getInt(),
                            BungeeMessages.FALLBACK_FADE_OUT.getInt(),
                            BungeeMessages.FALLBACK_TITLE,
                            BungeeMessages.FALLBACK_SUB_TITLE,
                            serverInfo,
                            player),
                    BungeeMessages.FALLBACK_DELAY.getInt(), 0, TimeUnit.SECONDS);
        }

        Component legacy = LegacyComponentSerializer.legacySection().deserialize(reason);
        String plain = PlainTextComponentSerializer.plainText().serialize(legacy);

        BungeeMessages.KICKED_TO_LOBBY.sendList(player,
                new Placeholder("server", serverInfo.getName()),
                new Placeholder("reason", plain));

        if (plugin.isDebug()) {
            Utils.printDebug("Player: " + player.getName() + " moved to " + serverInfo.getName(), false);
        }

        ErrorHandler.add(Severity.WARNING, "[FALLBACK] Successfully moved player " + player.getName() + " to " + serverInfo.getName());
    }

    private void handleReconnect(ServerKickEvent event, String reason, String serverName, ProxiedPlayer player) {
        List<String> ignoredReasons = BungeeConfig.RECONNECT_IGNORED_REASONS.getStringList();

        if (shouldIgnore(reason, BungeeConfig.RECONNECT_IGNORED_REASONS.getStringList())) {
            player.disconnect(new TextComponent(reason));
            return;
        }

        boolean ignoredServer = BungeeConfig.RECONNECT_IGNORED_SERVERS.getStringList().contains(serverName);

        if (ignoredServer) {
            return;
        }

        UserConnection userConnection = (UserConnection) player;
        ServerConnection serverConnection = userConnection.getServer();
        ReconnectSession session = new ReconnectSession(userConnection, serverConnection, player.getUniqueId());

        if (!playerCacheManager.containsKey(player.getUniqueId())) {
            playerCacheManager.addIfAbsent(player.getUniqueId(), session);
        }

        session.onJoin();
        reconnectManager.onKick(session, serverConnection.getInfo());

        boolean clearTab = BungeeConfig.RECONNECT_CLEAR_TABLIST.getBoolean();

        if (clearTab) {
            userConnection.resetTabHeader();
        }

        boolean clearChat = BungeeConfig.CLEAR_CHAT_RECONNECT_JOIN.getBoolean();

        if (clearChat) {
            chatUtil.clearChat(player);
        }

        boolean usePhysicalServer = plugin.getReconnectServer() != null;

        if (usePhysicalServer) {
            event.setCancelled(true);
            event.setCancelServer(plugin.getReconnectServer());
        }

        ErrorHandler.add(Severity.WARNING, "[RECONNECT] Player " + player.getName() + " is reconnecting to " + serverConnection.getInfo().getName());
    }

    private int getPendingConnections(String serverName) {
        if (pendingConnections.get(serverName) == null) {
            return 0;
        }

        return pendingConnections.get(serverName).intValue();
    }

    private void incrementPendingConnections(String serverName) {
        pendingConnections.computeIfAbsent(serverName, key -> new LongAdder()).increment();
    }

    private void decrementPendingConnections(String serverName) {
        LongAdder adder = pendingConnections.get(serverName);
        if (adder != null) {
            adder.decrement();
        }
    }

    private boolean shouldHandle(ServerKickEvent event) {
        return event.getPlayer().isConnected() && event.getState() == ServerKickEvent.State.CONNECTED;
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
