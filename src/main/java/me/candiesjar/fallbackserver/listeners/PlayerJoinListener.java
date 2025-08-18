package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.config.VelocityMessages;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handler.ErrorHandler;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.kyori.adventure.text.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class PlayerJoinListener {

    private final FallbackServerVelocity plugin;
    private final PlayerCacheManager playerCacheManager;
    private final ServerTypeManager serverTypeManager;
    private final OnlineLobbiesManager onlineLobbiesManager;

    public PlayerJoinListener(FallbackServerVelocity plugin) {
        this.plugin = plugin;
        this.playerCacheManager = plugin.getPlayerCacheManager();
        this.onlineLobbiesManager = plugin.getOnlineLobbiesManager();
        this.serverTypeManager = plugin.getServerTypeManager();
    }

    @Subscribe
    public void onPlayerJoin(ServerPreConnectEvent event) {
        Player player = event.getPlayer();

        if (playerCacheManager.containsKey(player.getUniqueId())) {
            return;
        }

        RegisteredServer previous = event.getPreviousServer();

        if (!(previous == null)) {
            return;
        }

        String groupName = VelocityConfig.JOIN_BALANCING_GROUP.get(String.class);
        String group = serverTypeManager.get(groupName) == null ? "default" : VelocityConfig.JOIN_BALANCING_GROUP.get(String.class);

        List<RegisteredServer> lobbies = Lists.newArrayList(onlineLobbiesManager.get(group));
        lobbies.removeIf(Objects::isNull);

        boolean useMaintenance = plugin.isMaintenance();

        if (useMaintenance) {
            lobbies.removeIf(ServerManager::checkMaintenance);
        }

        if (lobbies.isEmpty()) {
            player.disconnect(Component.text(ChatUtil.getFormattedString(VelocityMessages.NO_SERVER, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)))));
            return;
        }

        lobbies.sort(Comparator.comparingInt(server -> server.getPlayersConnected().size()));
        RegisteredServer target = lobbies.get(0);

        if (plugin.isDebug()) {
            Utils.printDebug("[JOIN SORTING] Player " + player.getUsername() + " is being sent to " + target.getServerInfo().getName(), false);
        }

        ErrorHandler.add(Severity.INFO, "[JOIN SORTING] Player " + player.getUsername() + " is being sent to " + target.getServerInfo().getName());

        event.setResult(ServerPreConnectEvent.ServerResult.allowed(target));
    }
}
