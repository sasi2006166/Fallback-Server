package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import net.md_5.bungee.api.CommandSender;

public class StatusSubCommand implements SubCommand {

    // Like Pokemon games, code is here, but not activated.

    @Override
    public String getPermission() {
        return BungeeConfig.STATUS_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return BungeeConfig.STATUS_COMMAND.getBoolean();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {



    }
}
