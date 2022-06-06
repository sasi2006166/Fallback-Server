package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(final ServerConnectEvent event) {
        final ProxiedPlayer player = event.getPlayer();

        if (!player.hasPermission(BungeeConfig.ADMIN_PERMISSION.getString())) {
            return;
        }

        if (Utils.isUpdateAvailable()) {
            BungeeMessages.NEW_UPDATE.sendList(player,
                    new PlaceHolder("old_version", FallbackServerBungee.getInstance().getVersion()),
                    new PlaceHolder("new_version", Utils.getRemoteVersion()));
        }

    }
}
