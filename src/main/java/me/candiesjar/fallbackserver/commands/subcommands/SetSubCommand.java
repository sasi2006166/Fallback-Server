package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class SetSubCommand implements SubCommand {

    @Override
    public String getPermission() {
        return BungeeConfig.SET_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return BungeeConfig.SET_COMMAND.getBoolean();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {

        if (arguments.length < 2) {
            sender.sendMessage(new TextComponent());
            return;
        }



    }
}
