package me.candiesjar.fallbackserver.commands.interfaces;

import com.velocitypowered.api.command.CommandSource;

public interface SubCommand {

    String getPermission();

    boolean isEnabled();

    void perform(CommandSource commandSource, String[] args);

}
