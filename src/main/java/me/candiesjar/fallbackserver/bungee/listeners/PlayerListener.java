package me.candiesjar.fallbackserver.bungee.listeners;

import me.candiesjar.fallbackserver.bungee.enums.BungeeMessages;
import me.candiesjar.fallbackserver.bungee.utils.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(ServerConnectEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        if (Utils.getUpdates())
            player.sendMessage(new TextComponent(BungeeMessages.NEW_UPDATE.getFormattedString()
                    .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())));
    }
}
