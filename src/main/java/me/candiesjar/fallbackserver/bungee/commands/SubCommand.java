package me.candiesjar.fallbackserver.bungee.commands;

import me.candiesjar.fallbackserver.bungee.enums.SubCommandType;
import net.md_5.bungee.api.CommandSender;

public interface SubCommand {

    SubCommandType getType();

    String getPermission();

    void perform(CommandSender sender, String[] arguments);

}
