package me.candiesjar.fallbackserver.bungee.commands.interfaces;

import net.md_5.bungee.api.CommandSender;

public interface SubCommand {

    String getPermission();

    boolean isEnabled();

    void perform(CommandSender sender, String[] arguments);

}
