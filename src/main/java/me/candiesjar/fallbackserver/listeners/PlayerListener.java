package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.utils.VelocityUtils;

public class PlayerListener {

    @Subscribe
    public void onPlayerJoin(ServerConnectedEvent event) {

        final Player player = event.getPlayer();

        if (!player.hasPermission(VelocityConfig.ADMIN_PERMISSION.get(String.class))) {
            return;
        }

        if (VelocityUtils.isUpdateAvailable()) {
            VelocityMessages.sendList(player, VelocityMessages.NEW_UPDATE.getStringList());
        }

    }

}
