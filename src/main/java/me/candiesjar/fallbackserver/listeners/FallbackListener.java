package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.api.FallbackAPI;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.utils.ServerUtils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import net.md_5.bungee.api.ProxyServer;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class FallbackListener implements Listener {

    private final FallbackServerBungee fallbackServerBungee;
    private final Map<String, LongAdder> pendingConnections = new ConcurrentHashMap<>();

    public FallbackListener(FallbackServerBungee fallbackServerBungee) {
        this.fallbackServerBungee = fallbackServerBungee;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKick(ServerKickEvent event) {

        ProxiedPlayer player = event.getPlayer();
        ServerInfo kickedFrom = event.getKickedFrom();

        if (!player.isConnected()) {
            return;
        }

        boolean isEmpty = event.getKickReasonComponent() == null;
        String reason = isEmpty ? "" : BaseComponent.toLegacyText(event.getKickReasonComponent());
        List<String> ignoredReasons = BungeeConfig.IGNORED_REASONS.getStringList();

        for (String word : ignoredReasons) {

            if (isEmpty) {
                break;
            }

            if (reason.contains(word)) {
                return;
            }

        }

        boolean useBlacklist = BungeeConfig.USE_BLACKLISTED_SERVERS.getBoolean();

        if (useBlacklist && BungeeConfig.BLACKLISTED_SERVERS_LIST.getStringList().contains(kickedFrom.getName())) {
            return;
        }

        event.setCancelled(true);

        FallingServer.removeServer(kickedFrom);
        List<FallingServer> lobbies = Lists.newArrayList(FallingServer.getServers().values());

        boolean hasMaintenance = fallbackServerBungee.isUseMaintenance();

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

        lobbies.sort(Comparator.comparingInt(server -> server.getServerInfo().getPlayers().size() + getPendingConnections(server.getServerInfo().getName())));

        ServerInfo serverInfo = lobbies.get(0).getServerInfo();

        event.setCancelServer(serverInfo);

        incrementPendingConnections(serverInfo.getName());
        fallbackServerBungee.getProxy().getScheduler().schedule(fallbackServerBungee, () -> decrementPendingConnections(serverInfo.getName()), 1, TimeUnit.SECONDS);

        BungeeMessages.KICKED_TO_LOBBY.sendList(player,
                new Placeholder("server", serverInfo.getName()),
                new Placeholder("reason", ChatUtil.color(reason)));

        boolean useTitle = BungeeMessages.USE_FALLBACK_TITLE.getBoolean();

        if (useTitle) {

            ProxyServer.getInstance().getScheduler().schedule(fallbackServerBungee, () -> TitleUtil.sendTitle(BungeeMessages.FALLBACK_FADE_IN.getInt(),
                            BungeeMessages.FALLBACK_STAY.getInt(),
                            BungeeMessages.FALLBACK_FADE_OUT.getInt(),
                            BungeeMessages.FALLBACK_TITLE,
                            BungeeMessages.FALLBACK_SUB_TITLE,
                            serverInfo,
                            player),
                    BungeeMessages.FALLBACK_DELAY.getInt(), 0, TimeUnit.SECONDS);
        }

        boolean notification = BungeeConfig.ADMIN_NOTIFICATION.getBoolean();

        if (notification) {

            ProxyServer.getInstance().getPlayers().stream().filter(all -> all != player);

        }

        ProxyServer.getInstance().getScheduler().runAsync(fallbackServerBungee, () -> fallbackServerBungee.getProxy().getPluginManager().callEvent(new FallbackAPI(player, kickedFrom, serverInfo, BaseComponent.toLegacyText(event.getKickReasonComponent()))));
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


