package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.handlers.FallbackReconnectHandler;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.objects.ServerType;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.ConditionUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class KickListener implements Listener {

    private final FallbackServerBungee plugin;
    private final ServerTypeManager serverTypeManager;
    private final OnlineLobbiesManager onlineLobbiesManager;
    private final Map<String, LongAdder> pendingConnections;

    public KickListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
        this.serverTypeManager = plugin.getServerTypeManager();
        this.onlineLobbiesManager = plugin.getOnlineLobbiesManager();
        this.pendingConnections = Maps.newConcurrentMap();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKick(ServerKickEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (!player.isConnected()) {
            return;
        }

        ServerKickEvent.State state = event.getState();

        if (state != ServerKickEvent.State.CONNECTED) {
            return;
        }

        ServerInfo kickedFrom = event.getKickedFrom();
        String kickedName = kickedFrom == null ? "ReconnectLimbo" : kickedFrom.getName();
        String group = ServerManager.getGroupByServer(kickedName) == null ? "default" : ServerManager.getGroupByServer(kickedName);
        boolean isEmpty = event.getReason() == null;
        String reason = isEmpty ? "Lost Connection" : BaseComponent.toLegacyText(event.getReason()).trim();
        ServerType serverType = serverTypeManager.get(group);

        if (serverType == null || kickedName.equalsIgnoreCase("ReconnectLimbo")) {
            handleFallback(event, kickedFrom, player, reason, kickedName);
            return;
        }

        boolean isReconnect = serverType.isReconnect();

        if (isReconnect) {
            handleReconnect(event, reason, kickedName, player);
            return;
        }

        handleFallback(event, kickedFrom, player, reason, kickedName);
    }

    private void handleFallback(ServerKickEvent event, ServerInfo kickedFrom, ProxiedPlayer player, String reason, String serverName) {
        List<String> ignoredReasons = BungeeConfig.IGNORED_REASONS.getStringList();

        if (shouldIgnore(reason, BungeeConfig.IGNORED_REASONS.getStringList())) {
            return;
        }

        boolean ignoredServer = BungeeConfig.USE_IGNORED_SERVERS.getBoolean() && BungeeConfig.IGNORED_SERVER_LIST.getStringList().contains(serverName);

        if (ignoredServer) {
            return;
        }

        event.setCancelled(true);

        String group = ServerManager.getGroupByServer(serverName) == null ? "default" : ServerManager.getGroupByServer(serverName);
        List<ServerInfo> lobbies = Lists.newArrayList(onlineLobbiesManager.get(group));
        lobbies.removeIf(Objects::isNull);
        lobbies.remove(kickedFrom);

        boolean useMaintenance = plugin.isMaintenance();

        if (useMaintenance) {
            lobbies.removeIf(ServerManager::checkMaintenance);
        }

        if (lobbies.isEmpty()) {
            if (reason.isEmpty()) {
                player.disconnect(new TextComponent(ChatUtil.getFormattedString(BungeeMessages.NO_SERVER)));
                return;
            }
            player.disconnect(new TextComponent(reason));
            return;
        }

        lobbies.sort(Comparator.comparingInt(server -> server.getPlayers().size() + getPendingConnections(server.getName())));
        ServerInfo serverInfo = lobbies.get(0);

        event.setCancelServer(serverInfo);
        incrementPendingConnections(serverInfo.getName());

        plugin.getProxy().getScheduler().schedule(plugin, () -> decrementPendingConnections(serverInfo.getName()), 1, TimeUnit.SECONDS);

        boolean clearChat = BungeeConfig.CLEAR_CHAT_FALLBACK.getBoolean();

        if (clearChat) {
            ChatUtil.clearChat(player);
        }

        boolean useTitle = BungeeMessages.USE_FALLBACK_TITLE.getBoolean();

        if (useTitle) {
            plugin.getProxy().getScheduler().schedule(plugin, () -> TitleUtil.sendTitle(BungeeMessages.FALLBACK_FADE_IN.getInt(),
                            BungeeMessages.FALLBACK_STAY.getInt(),
                            BungeeMessages.FALLBACK_FADE_OUT.getInt(),
                            BungeeMessages.FALLBACK_TITLE,
                            BungeeMessages.FALLBACK_SUB_TITLE,
                            serverInfo,
                            player),
                    BungeeMessages.FALLBACK_DELAY.getInt(), 0, TimeUnit.SECONDS);
        }

        BungeeMessages.KICKED_TO_LOBBY.sendList(player,
                new Placeholder("server", serverInfo.getName()),
                new Placeholder("reason", ChatUtil.formatColor(reason)));
    }

    private void handleReconnect(ServerKickEvent event, String reason, String serverName, ProxiedPlayer player) {
        List<String> ignoredReasons = BungeeConfig.RECONNECT_IGNORED_REASONS.getStringList();

        if (shouldIgnore(reason, BungeeConfig.RECONNECT_IGNORED_REASONS.getStringList())) {
            return;
        }

        boolean ignoredServer = BungeeConfig.RECONNECT_IGNORED_SERVERS.getStringList().contains(serverName);

        if (ignoredServer) {
            return;
        }

        UserConnection userConnection = (UserConnection) player;
        ServerConnection serverConnection = userConnection.getServer();
        FallbackReconnectHandler task = plugin.getPlayerCacheManager().get(player.getUniqueId());

        if (task == null) {
            plugin.getPlayerCacheManager().put(player.getUniqueId(), task = new FallbackReconnectHandler(userConnection, serverConnection, userConnection.getUniqueId()));
        }

        boolean clearTab = BungeeConfig.RECONNECT_CLEAR_TABLIST.getBoolean();

        if (clearTab) {
            userConnection.resetTabHeader();
        }

        boolean clearChat = BungeeConfig.CLEAR_CHAT_RECONNECT_JOIN.getBoolean();

        if (clearChat) {
            ChatUtil.clearChat(player);
        }

        task.onJoin();

        boolean usePhysicalServer = plugin.getReconnectServer() != null;

        if (usePhysicalServer) {
            event.setCancelled(true);
            event.setCancelServer(plugin.getReconnectServer());
        }
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

    private boolean shouldIgnore(String reason, List<String> ignoredReasons) {
        return ConditionUtil.checkReason(ignoredReasons, reason);
    }
}
