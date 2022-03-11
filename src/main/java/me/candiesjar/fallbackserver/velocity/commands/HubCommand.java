package me.candiesjar.fallbackserver.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.velocity.enums.VelocityMessages;

public class HubCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(VelocityMessages.colorize(VelocityMessages.NOT_PLAYER.getString()));
            return;
        }
        Player player = (Player) invocation;
    }
}
