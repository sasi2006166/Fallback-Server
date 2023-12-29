package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.utils.Utils;

import java.util.UUID;

@RequiredArgsConstructor
public class ServerSwitchListener {

    private final FallbackServerVelocity plugin;

    @Subscribe
    public void onServerSwitch(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        boolean isReconnecting = plugin.getPlayerCacheManager().containsKey(uuid);

        if (isReconnecting) {
            plugin.cancelReconnect(uuid);
        }

    }

}
