package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import net.kyori.adventure.text.Component;

public class FallbackListener {


    @Subscribe
    public void onPlayerKick(KickedFromServerEvent event) {

        Player player = event.getPlayer();
        String kickedServer = event.getServer().toString();

        if (!player.isActive()) {
            return;
        }




    }

}
