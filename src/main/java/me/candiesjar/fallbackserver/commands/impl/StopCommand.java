package me.candiesjar.fallbackserver.commands.impl;

import com.velocitypowered.api.command.CommandSource;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;

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
    public void perform(CommandSource commandSource, String[] args) {

    }
}
