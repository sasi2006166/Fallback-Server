package me.candiesjar.fallbackserver.bungee.listeners;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.enums.ConfigFields;
import me.candiesjar.fallbackserver.bungee.enums.MessagesFields;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {

    @EventHandler(priority = 64)
    public void onChat(ChatEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String message = event.getMessage();
        String playerServer = player.getServer().getInfo().getName();
        if (message.length() > 1) {
            String[] args = message.split(" ");
            message = args[0];
        }
        boolean checkMessage = checkMessage(message, playerServer);
        if (!player.hasPermission(ConfigFields.PERMISSION.getString()))
            if (checkMessage) {
                event.setCancelled(true);
                player.sendMessage(new TextComponent(MessagesFields.BLOCKED_COMMAND.getFormattedString()
                        .replace("%prefix%", MessagesFields.PREFIX.getFormattedString())));
            }
    }

    private boolean checkMessage(String message, String name) {
        for (String text : FallbackServerBungee.getInstance().getConfig().getStringList("Hub.disabled_servers." + name)) {
            text = "/" + text;
            if (text.equalsIgnoreCase(message)) {
                return true;
            }
        }
        return false;
    }
}
