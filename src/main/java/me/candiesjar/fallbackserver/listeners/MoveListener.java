package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.events.FallbackEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MoveListener implements Listener {

    @EventHandler
    public void onServerMove(FallbackEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ServerInfo serverInfo = event.getActualServer();
        String reason = event.getReason();

        if (!player.isConnected()) {
            disconnect(player, reason);
            return;
        }
        
        

    }

    private void disconnect(ProxiedPlayer player, String reason) {
        player.disconnect(new TextComponent(reason));
    }

}
