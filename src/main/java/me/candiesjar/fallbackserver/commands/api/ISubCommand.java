package me.candiesjar.fallbackserver.commands.api;

import net.md_5.bungee.api.CommandSender;

public interface ISubCommand {

    String getPermission();

    boolean isEnabled();

    void perform(CommandSender sender, String[] arguments);

}
