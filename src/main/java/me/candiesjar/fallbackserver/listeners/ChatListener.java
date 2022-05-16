package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.VelocityUtils;
import net.kyori.adventure.text.Component;

public class ChatListener {

    @Subscribe
    public void onPlayerChat(final CommandExecuteEvent event) {

        if (!(event.getCommandSource() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getCommandSource();
        final String playerServer = player.getCurrentServer().get().getServerInfo().getName();
        String command = event.getCommand();

        if (player.hasPermission(VelocityConfig.ADMIN_PERMISSION.get(String.class))) {
            return;
        }

        if (command.length() > 1) {
            String[] args = command.split(" ");
            command = args[0];
        }

        final boolean checkMessage = VelocityUtils.checkMessage(command, playerServer);

        if (checkMessage) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            VelocityMessages.BLOCKED_COMMAND.send(player, new PlaceHolder("prefix", VelocityMessages.PREFIX.color()));
        }
    }
}
