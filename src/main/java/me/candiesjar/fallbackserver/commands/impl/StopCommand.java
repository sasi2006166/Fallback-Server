package me.candiesjar.fallbackserver.commands.impl;

import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import net.md_5.bungee.api.CommandSender;

public class StopCommand implements ISubCommand {
    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {

    }
}
