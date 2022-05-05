package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.utils.VelocityUtils;

public class ChatListener {

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {

        final Player player = event.getPlayer();
        final String playerServer = player.getCurrentServer().get().getServerInfo().getName();
        String message = event.getMessage();

        if (!message.startsWith("/")) {
            return;
        }

        if (player.hasPermission(VelocityConfig.ADMIN_PERMISSION.get(String.class))) {
            return;
        }

        if (message.length() > 1) {
            String[] args = message.split(" ");
            message = args[0];
        }

        final boolean checkMessage = VelocityUtils.checkMessage(message, playerServer);

        if (checkMessage) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            player.sendMessage(VelocityMessages.colorize(VelocityMessages.BLOCKED_COMMAND.get(String.class)
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
        }

    }

}
