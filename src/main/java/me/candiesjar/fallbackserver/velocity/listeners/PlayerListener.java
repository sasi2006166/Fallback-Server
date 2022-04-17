package me.candiesjar.fallbackserver.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import me.candiesjar.fallbackserver.velocity.utils.VelocityUtils;

public class PlayerListener {


    @Subscribe
    public void onPlayerJoin(ServerConnectedEvent event) {

        final Player player = event.getPlayer();

        if (VelocityUtils.isUpdateAvailable()) {

        }


    }



}
