package me.candiesjar.fallbackserver.commands.api;

import com.velocitypowered.api.command.CommandSource;

public interface ISubCommand {

    String getPermission();

    boolean isEnabled();

    void perform(CommandSource commandSource, String[] args);

}
