package me.candiesjar.fallbackserver.bungee.commands.subCommands;

import me.candiesjar.fallbackserver.bungee.commands.SubCommand;
import me.candiesjar.fallbackserver.bungee.enums.SubCommandType;
import net.md_5.bungee.api.CommandSender;

public class SetSubCommand implements SubCommand {

    // Like Pokemon games, code is here, but not activated.

    @Override
    public SubCommandType getType() {
        return SubCommandType.UNIVERSAL;
    }

    @Override
    public String getPermission() {
        return "fallback.internal.test";
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {




        /* TODO 3.1 Update */


    }
}
