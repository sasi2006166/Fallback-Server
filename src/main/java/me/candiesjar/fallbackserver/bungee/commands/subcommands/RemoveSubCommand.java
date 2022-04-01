package me.candiesjar.fallbackserver.bungee.commands.subcommands;

import me.candiesjar.fallbackserver.bungee.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.bungee.enums.BungeeConfig;
import net.md_5.bungee.api.CommandSender;

public class RemoveSubCommand implements SubCommand {

    // Like Pokemon games, code is here, but not activated.

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
