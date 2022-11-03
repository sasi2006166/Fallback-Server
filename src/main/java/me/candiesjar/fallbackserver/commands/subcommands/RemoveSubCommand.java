package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import net.md_5.bungee.api.CommandSender;

public class RemoveSubCommand implements SubCommand {

    @Override
    public String getPermission() {
        return BungeeConfig.REMOVE_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return BungeeConfig.REMOVE_COMMAND.getBoolean();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {

        if (arguments.length < 2) {
            return;
        }

    }
}
