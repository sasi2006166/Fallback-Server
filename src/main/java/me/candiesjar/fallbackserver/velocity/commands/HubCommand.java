package me.candiesjar.fallbackserver.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.candiesjar.fallbackserver.velocity.utils.ColorUtil;
import me.candiesjar.fallbackserver.velocity.utils.VelocityFields;

public class HubCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(ColorUtil.colorize(VelocityFields.NOT_PLAYER.getString()));
            return;
        }
        Player player = (Player) invocation;
    }
}
