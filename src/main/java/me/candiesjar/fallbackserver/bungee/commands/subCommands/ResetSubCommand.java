package me.candiesjar.fallbackserver.bungee.commands.subCommands;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.commands.SubCommand;
import me.candiesjar.fallbackserver.bungee.enums.BungeeConfig;
import me.candiesjar.fallbackserver.bungee.enums.BungeeMessages;
import me.candiesjar.fallbackserver.bungee.enums.SubCommandType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class ResetSubCommand implements SubCommand {

    // Like Pokemon games, code is here, but not activated.

    private boolean restoreConfig = false;
    private boolean restoreMessages = false;

    @Override
    public SubCommandType getType() {
        return SubCommandType.UNIVERSAL;
    }

    @Override
    public String getPermission() {
        return BungeeConfig.RESET_COMMAND_PERMISSION.getString();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {
        if (arguments.length < 2) {
            sender.sendMessage(new TextComponent("Messages or config?"));
            return;
        }
        switch (arguments[1]) {
            case "config":
                if (!restoreConfig) {
                    for (String message : BungeeMessages.CONFIGURATION_WARN.getStringList()) {
                        sender.sendMessage(new TextComponent(BungeeMessages.getFormattedString(message)));
                    }
                    restoreConfig = true;
                    return;
                }
                FallbackServerBungee.getInstance().reCreateConfig("config.yml");
                sender.sendMessage(new TextComponent(BungeeMessages.CONFIGURATION_RESTORED.getFormattedString()
                        .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())));
                restoreConfig = false;
                break;
            case "messages":
                if (!restoreMessages) {
                    for (String message : BungeeMessages.MESSAGES_WARN.getStringList()) {
                        sender.sendMessage(new TextComponent(BungeeMessages.getFormattedString(message)));
                    }
                    restoreMessages = true;
                    return;
                }
                FallbackServerBungee.getInstance().reCreateConfig("messages.yml");
                sender.sendMessage(new TextComponent(BungeeMessages.MESSAGES_RESTORED.getFormattedString()
                        .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())));
                break;
        }
    }
}
