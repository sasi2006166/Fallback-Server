package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class PlayerJoinListener implements Listener {

    private final FallbackServerBungee plugin;
    private final OnlineLobbiesManager onlineLobbiesManager;
    private final ServerTypeManager serverTypeManager;

    public PlayerJoinListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
        this.onlineLobbiesManager = plugin.getOnlineLobbiesManager();
        this.serverTypeManager = plugin.getServerTypeManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(ServerConnectEvent event) {
        if (!event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            return;
        }

        ProxiedPlayer player = event.getPlayer();
        String groupName = BungeeConfig.JOIN_BALANCING_GROUP.getString();
        String group = serverTypeManager.get(groupName) == null ? "default" : BungeeConfig.JOIN_BALANCING_GROUP.getString();

        List<ServerInfo> lobbies = onlineLobbiesManager.get(group);
        lobbies.removeIf(Objects::isNull);

        boolean useMaintenance = plugin.isMaintenance();

        if (useMaintenance) {
            lobbies.removeIf(ServerManager::checkMaintenance);
        }

        if (lobbies.isEmpty()) {
            player.disconnect(new TextComponent(ChatUtil.getFormattedString(BungeeMessages.NO_SERVER)
                    .replace("%prefix%", ChatUtil.getFormattedString(BungeeMessages.PREFIX))));
            return;
        }

        lobbies.sort(Comparator.comparingInt(server -> server.getPlayers().size()));
        ServerInfo serverInfo = lobbies.get(0);

        if (plugin.isDebug()) {
            Utils.printDebug("[JOIN SORTING] Player " + player.getName() + " is being sent to " + serverInfo.getName(), false);
        }

        ErrorHandler.add(Severity.INFO, "[JOIN SORTING] Player " + player.getName() + " is being sent to " + serverInfo.getName());

        event.setTarget(serverInfo);
    }
}
