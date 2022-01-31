package me.candiesjar.fallbackserver.bungee.commands.subCommands;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.commands.SubCommand;
import me.candiesjar.fallbackserver.bungee.enums.ConfigFields;
import me.candiesjar.fallbackserver.bungee.enums.MessagesFields;
import me.candiesjar.fallbackserver.bungee.enums.SubCommandType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class ReloadSubCommand implements SubCommand {

    @Override
    public SubCommandType getType() {
        return SubCommandType.UNIVERSAL;
    }

    @Override
    public String getPermission() {
        return ConfigFields.RELOAD_PERMISSION.getString();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {
        FallbackServerBungee.getInstance().reloadConfig();
        sender.sendMessage(new TextComponent(MessagesFields.RELOAD_MESSAGE.getFormattedString()
                .replace("%prefix%", MessagesFields.PREFIX.getFormattedString())));
    }
}
