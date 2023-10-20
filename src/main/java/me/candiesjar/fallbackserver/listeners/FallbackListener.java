package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.utils.ConditionUtil;
import me.candiesjar.fallbackserver.utils.Utils;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class FallbackListener implements Listener {

    private final FallbackServerBungee plugin;
    private final HashMap<String, LongAdder> pendingConnections;

    public FallbackListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
        this.pendingConnections = Maps.newHashMap();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKick(ServerKickEvent event) {

        ProxiedPlayer player = event.getPlayer();
        ServerInfo kickedFrom = event.getKickedFrom();
        ServerKickEvent.State state = event.getState();

        boolean isEmpty = event.getKickReasonComponent() == null;
        String reason = isEmpty ? "" : BaseComponent.toLegacyText(event.getKickReasonComponent());

        boolean canContinue = ConditionUtil.preChecks(player, state, reason, false);

        if (canContinue) {
            return;
        }

        if (checkIgnoredServers(kickedFrom.getName())) {
            return;
        }

        event.setCancelled(true);

        FallingServer.removeServer(kickedFrom);

        List<FallingServer> lobbies = Lists.newArrayList();
        FallingServer.getServers().values().forEach(fallingServer -> {
            if (fallingServer != null && fallingServer.getServerInfo() != null) {
                lobbies.add(fallingServer);
            }
        });

        boolean hasMaintenance = plugin.isMaintenance();

        if (hasMaintenance) {
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

        for (FallingServer fallingServer : lobbies) {
            try {
                Utils.printDebug("Lobby: " + fallingServer.getServerInfo().getName() + " Players: " + fallingServer.getServerInfo().getPlayers().size(), true);
            } catch (NullPointerException e) {
                Utils.printDebug("Lobby: " + fallingServer + " gave error", true);
            }
        }

        lobbies.sort(Comparator.comparingInt(server -> server.getServerInfo().getPlayers().size() + getPendingConnections(server.getServerInfo().getName())));

        ServerInfo serverInfo = lobbies.get(0).getServerInfo();

        player.connect(serverInfo);

        incrementPendingConnections(serverInfo.getName());
        plugin.getProxy().getScheduler().schedule(plugin, () -> decrementPendingConnections(serverInfo.getName()), 2, TimeUnit.SECONDS);

        boolean clearChat = BungeeConfig.CLEAR_CHAT_FALLBACK.getBoolean();

        if (clearChat) {
            ChatUtil.clearChat(player);
        }

        BungeeMessages.KICKED_TO_LOBBY.sendList(player,
                new Placeholder("server", serverInfo.getName()),
                new Placeholder("reason", ChatUtil.color(reason)));

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

    }

    private boolean checkIgnoredServers(String serverName) {
        return BungeeConfig.USE_IGNORED_SERVERS.getBoolean() && BungeeConfig.IGNORED_SERVER_LIST.getStringList().contains(serverName);
    }

    private int getPendingConnections(String serverName) {
        return pendingConnections.getOrDefault(serverName, new LongAdder()).intValue();
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


