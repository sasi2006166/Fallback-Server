package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.tasks.ReconnectTask;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final FallbackServerBungee plugin;

    public PlayerListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
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
                    new Placeholder("old_version", FallbackServerBungee.getInstance().getVersion()),
                    new Placeholder("new_version", Utils.getRemoteVersion()));
        }

    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        ReconnectTask reconnectTask = PlayerCacheManager.getInstance().get(uuid);

        if (reconnectTask != null) {
            plugin.cancelReconnect(uuid);
        }

    }
}
