package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.VelocityUtils;

public class PlayerListener {

    private final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    @Subscribe
    public void onPlayerJoin(final ServerConnectedEvent event) {

        final Player player = event.getPlayer();

        if (!player.hasPermission(VelocityConfig.ADMIN_PERMISSION.get(String.class))) {
            return;
        }

        if (VelocityUtils.isUpdateAvailable()) {
            VelocityMessages.NEW_UPDATE.sendList(player,
                    new PlaceHolder("old_version", instance.getVersion().get()),
                    new PlaceHolder("new_version", VelocityUtils.getRemoteVersion()));
        }

    }

}
