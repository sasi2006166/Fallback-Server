package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class StatusSubCommand implements SubCommand {

    // Like Pokemon games, code is here, but not activated.

    @Override
    public String getPermission() {
        return BungeeConfig.STATUS_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return BungeeConfig.STATUS_COMMAND.getBoolean();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {

        BungeeMessages.STATS_COMMAND.sendList(sender,
                new PlaceHolder("player", sender.getName()),
                new PlaceHolder("total_players", "" + ProxyServer.getInstance().getPlayers().size()),
                new PlaceHolder("total_memory", "" + Runtime.getRuntime().totalMemory()),
                new PlaceHolder("max_memory", "" + Runtime.getRuntime().maxMemory()),
                new PlaceHolder("cpu_cores", "" + Runtime.getRuntime().availableProcessors()),
                new PlaceHolder("version", FallbackServerBungee.getInstance().getVersion()));



    }
}
