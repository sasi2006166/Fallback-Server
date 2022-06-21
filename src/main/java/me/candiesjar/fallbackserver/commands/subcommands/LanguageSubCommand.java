package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import net.md_5.bungee.api.CommandSender;

public class LanguageSubCommand implements SubCommand {

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {

    }
    
}
