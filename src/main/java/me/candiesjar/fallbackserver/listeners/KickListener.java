package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.objects.ServerType;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.ConditionUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import net.kyori.adventure.text.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class KickListener {

    private final FallbackServerVelocity plugin;
    private final ServerTypeManager serverTypeManager;
    private final OnlineLobbiesManager onlineLobbiesManager;
    private final Map<String, LongAdder> pendingConnections;

    public KickListener(FallbackServerVelocity plugin) {
        this.plugin = plugin;
        this.serverTypeManager = plugin.getServerTypeManager();
        this.onlineLobbiesManager = plugin.getOnlineLobbiesManager();
        this.pendingConnections = Maps.newConcurrentMap();
    }

    @Subscribe(priority = Short.MAX_VALUE)
    public void onPlayerKick(KickedFromServerEvent event) {

        if (event.kickedDuringServerConnect()) {
            return;
        }

        Player player = event.getPlayer();
        RegisteredServer kickedFrom = event.getServer();
        String kickedName = kickedFrom.getServerInfo() == null ? "" : kickedFrom.getServerInfo().getName();
        boolean isEmpty = event.getServerKickReason().isEmpty();
        String kickReasonString = isEmpty ? "Lost Connection" : ChatUtil.componentToString(event.getServerKickReason().get());
        String group = ServerManager.getGroupByServer(kickedName) == null ? "default" : ServerManager.getGroupByServer(kickedName);
        ServerType serverType = serverTypeManager.get(group);

        if (kickedName.equalsIgnoreCase("FallbackLimbo")) {
            handleFallback(event, kickedFrom, player, kickReasonString, kickedName, true);
            return;
        }

        boolean physical = VelocityConfig.RECONNECT_USE_PHYSICAL.get(Boolean.class);
        if (physical && kickedName.equalsIgnoreCase(VelocityConfig.RECONNECT_PHYSICAL_SERVER.get(String.class))) {
            handleFallback(event, kickedFrom, player, kickReasonString, kickedName, true);
            return;
        }

        if (serverType == null) {
            handleFallback(event, kickedFrom, player, kickReasonString, kickedName, false);
            return;
        }

        boolean isReconnect = serverType.isReconnect();

        if (isReconnect && plugin.isReconnect()) {
            return;
        }

        handleFallback(event, kickedFrom, player, kickReasonString, kickedName, false);
    }

    private void handleFallback(KickedFromServerEvent event, RegisteredServer kickedFrom, Player player, String kickReasonString, String kickedName, boolean reconnect) {
        List<String> ignoredReasons = VelocityConfig.IGNORED_REASONS.getStringList();

        if (shouldIgnore(kickReasonString, ignoredReasons)) {
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(ChatUtil.color(kickReasonString))));
            return;
        }

        boolean ignoredServer = VelocityConfig.USE_IGNORED_SERVERS.get(Boolean.class) && VelocityConfig.IGNORED_SERVERS_LIST.getStringList().contains(kickedName);

        if (ignoredServer) {
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(ChatUtil.color(kickReasonString))));
            return;
        }

        String group = ServerManager.getGroupByServer(kickedName) == null ? "default" : ServerManager.getGroupByServer(kickedName);
        List<RegisteredServer> lobbies = Lists.newArrayList(onlineLobbiesManager.get(group));
        lobbies.removeIf(Objects::isNull);
        lobbies.remove(kickedFrom);

        boolean useMaintenance = plugin.isMaintenance();

        if (useMaintenance) {
            lobbies.removeIf(ServerManager::checkMaintenance);
        }

        if (lobbies.isEmpty()) {
            if (kickReasonString.isEmpty()) {
                String disconnectMessage = VelocityMessages.NO_SERVER.get(String.class).replace("%prefix%", ChatUtil.getFormattedString(VelocityMessages.PREFIX));
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(ChatUtil.color(disconnectMessage))));
                return;
            }
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(ChatUtil.color(kickReasonString))));
            return;
        }

        lobbies.sort(Comparator.comparingInt(server -> server.getPlayersConnected().size() + getPendingConnections(server.getServerInfo().getName())));
        RegisteredServer registeredServer = lobbies.get(0);

        event.setResult(KickedFromServerEvent.RedirectPlayer.create(registeredServer));
        incrementPendingConnections(registeredServer.getServerInfo().getName());

        plugin.getServer().getScheduler().buildTask(plugin, () -> decrementPendingConnections(registeredServer.getServerInfo().getName()))
                .delay(1, TimeUnit.SECONDS)
                .schedule();

        if (!reconnect) {
            boolean clearChat = VelocityConfig.CLEAR_CHAT_FALLBACK.get(Boolean.class);

            if (clearChat) {
                ChatUtil.clearChat(player);
            }
        }

        boolean useTitle = VelocityMessages.USE_FALLBACK_TITLE.get(Boolean.class);

        if (useTitle) {
            plugin.getServer().getScheduler().buildTask(plugin, () -> TitleUtil.sendTitle(
                            VelocityMessages.FALLBACK_FADE_IN.get(Integer.class),
                            VelocityMessages.FALLBACK_STAY.get(Integer.class),
                            VelocityMessages.FALLBACK_FADE_OUT.get(Integer.class),
                            VelocityMessages.FALLBACK_TITLE.get(String.class),
                            VelocityMessages.FALLBACK_SUB_TITLE.get(String.class),
                            registeredServer,
                            player)).delay(VelocityMessages.FALLBACK_DELAY.get(Integer.class) + 1, TimeUnit.SECONDS)
                    .schedule();
        }

        if (!reconnect) {
            VelocityMessages.KICKED_TO_LOBBY.sendList(player,
                    new Placeholder("server", registeredServer.getServerInfo().getName()),
                    new Placeholder("reason", ChatUtil.color(kickReasonString)));
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
