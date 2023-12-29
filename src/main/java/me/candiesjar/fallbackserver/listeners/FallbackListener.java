package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.utils.ConditionUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import me.candiesjar.fallbackserver.utils.server.ServerUtils;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class FallbackListener implements Listener {

    private final FallbackServerBungee plugin;
    private final Map<String, LongAdder> pendingConnections;

    public FallbackListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
        this.pendingConnections = Maps.newLinkedHashMap();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKick(ServerKickEvent event) {

        ProxiedPlayer player = event.getPlayer();

        if (!player.isConnected()) {
            return;
        }

        ServerInfo kickedFrom = event.getKickedFrom();
        ServerKickEvent.State state = event.getState();

        if (state != ServerKickEvent.State.CONNECTED) {
            return;
        }

        boolean isEmpty = event.getKickReasonComponent() == null;
        String reason = isEmpty ? "" : BaseComponent.toLegacyText(event.getKickReasonComponent());
        List<String> ignoredReasons = BungeeConfig.IGNORED_REASONS.getStringList();

        boolean matches = ConditionUtil.checkReason(ignoredReasons, reason);

        if (matches) {
            return;
        }

        if (checkIgnoredServers(kickedFrom.getName())) {
            return;
        }

        event.setCancelled(true);

        FallingServer.removeServer(kickedFrom);
        List<FallingServer> lobbies = Lists.newArrayList(FallingServer.getServers().values());

        boolean useMaintenance = plugin.isMaintenance();

        if (useMaintenance) {
            lobbies.removeIf(fallingServer -> ServerUtils.checkMaintenance(fallingServer.getServerInfo()));
        }

        if (lobbies.isEmpty()) {
            if (isEmpty) {
                player.disconnect(new TextComponent(ChatUtil.getFormattedString(BungeeMessages.NO_SERVER)));
                return;
            }
            player.disconnect(new TextComponent(reason));
            return;
        }

        lobbies.removeIf(fallingServer -> fallingServer.getServerInfo() == null);
        lobbies.sort(Comparator.comparingInt(server -> server.getServerInfo().getPlayers().size() + getPendingConnections(server.getServerInfo().getName())));
        ServerInfo serverInfo = lobbies.get(0).getServerInfo();

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
                new Placeholder("reason", ChatUtil.color(reason)));
    }

    private boolean checkIgnoredServers(String serverName) {
        return BungeeConfig.USE_IGNORED_SERVERS.getBoolean() && BungeeConfig.IGNORED_SERVER_LIST.getStringList().contains(serverName);
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
}

