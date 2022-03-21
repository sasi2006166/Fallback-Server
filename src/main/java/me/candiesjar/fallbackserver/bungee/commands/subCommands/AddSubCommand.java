package me.candiesjar.fallbackserver.bungee.commands.subCommands;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.commands.SubCommand;
import me.candiesjar.fallbackserver.bungee.enums.BungeeConfig;
import me.candiesjar.fallbackserver.bungee.enums.BungeeMessages;
import me.candiesjar.fallbackserver.bungee.enums.SubCommandType;
import me.candiesjar.fallbackserver.bungee.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

public class AddSubCommand implements SubCommand {

    // Like Pokemon games, code is here, but not activated.

    @Override
    public SubCommandType getType() {
        return SubCommandType.UNIVERSAL;
    }

    @Override
    public String getPermission() {
            return BungeeConfig.ADD_COMMAND_PERMISSION.getString();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {
        if (arguments.length < 2) {
            sender.sendMessage(new TextComponent(BungeeMessages.EMPTY_SERVER.getFormattedString()
                    .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())));
            return;
        }
        if (FallbackServerBungee.getInstance().getServerList().contains(arguments[1])) {
            sender.sendMessage(new TextComponent(new TextComponent(BungeeMessages.SERVER_IS_ADDED.getFormattedString()
                    .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())
                    .replace("%server%", arguments[1]))));
            return;
        }
        if (!ProxyServer.getInstance().getConfig().getServersCopy().containsKey(arguments[1])) {
            sender.sendMessage(new TextComponent(BungeeMessages.SERVER_NOT_AVAILABLE.getFormattedString()
                    .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())
                    .replace("%server%", arguments[1])));
            return;
        }
        Utils.writeToServerList("Hub.server_list", arguments[1]);
        sender.sendMessage(new TextComponent(BungeeMessages.SERVER_ADDED.getFormattedString()
                .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())
                .replace("%server%", arguments[1])));
    }
}
