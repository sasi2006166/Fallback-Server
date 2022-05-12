package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class AddSubCommand implements SubCommand {

    // Like Pokemon games, code is here, but not activated.

    @Override
    public String getPermission() {
        return BungeeConfig.ADD_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return BungeeConfig.ADD_COMMAND.getBoolean();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {
        if (arguments.length < 2) {
            BungeeMessages.EMPTY_SERVER.send(sender, new PlaceHolder("prefix", FallbackServerBungee.getInstance().getPrefix()));
            return;
        }

        if (FallbackServerBungee.getInstance().getServerList().contains(arguments[1])) {
            BungeeMessages.SERVER_CONTAINED.send(sender, new PlaceHolder("server", arguments[1]));
            return;
        }

        if (!ProxyServer.getInstance().getConfig().getServersCopy().containsKey(arguments[1])) {
            BungeeMessages.UNAVAILABLE_SERVER.send(sender, new PlaceHolder("server", arguments[1]));
            return;
        }

        Utils.writeToServerList("Hub.server_list", arguments[1]);
        BungeeMessages.SERVER_ADDED.send(sender, new PlaceHolder("server", arguments[1]));
    }
}
