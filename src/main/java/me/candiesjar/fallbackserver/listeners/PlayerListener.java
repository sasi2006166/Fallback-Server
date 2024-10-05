package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.handlers.ReconnectHandler;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.ReconnectUtil;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final FallbackServerBungee plugin;
    private final PlayerCacheManager playerCacheManager;

    public PlayerListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
        this.playerCacheManager = plugin.getPlayerCacheManager();
    }

    @EventHandler
    public void onPlayerJoin(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (!player.hasPermission(BungeeConfig.ADMIN_PERMISSION.getString())) {
            return;
        }

        if (!event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            return;
        }

        if (Utils.isUpdateAvailable()) {
            BungeeMessages.NEW_UPDATE.sendList(player,
                    new Placeholder("old_version", plugin.getVersion()),
                    new Placeholder("new_version", Utils.getRemoteVersion()));
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        ReconnectHandler reconnectHandler = playerCacheManager.get(uuid);

        if (reconnectHandler != null) {
            ReconnectUtil.cancelReconnect(uuid);
        }
    }
}
