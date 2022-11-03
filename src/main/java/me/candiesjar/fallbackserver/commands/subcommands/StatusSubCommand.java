package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.Placeholder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class StatusSubCommand implements SubCommand {

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
                new Placeholder("player", sender.getName()),
                new Placeholder("total_players", "" + ProxyServer.getInstance().getPlayers().size()),
                new Placeholder("total_memory", "" + Runtime.getRuntime().totalMemory()),
                new Placeholder("max_memory", "" + Runtime.getRuntime().maxMemory()),
                new Placeholder("cpu_cores", "" + Runtime.getRuntime().availableProcessors()),
                new Placeholder("version", FallbackServerBungee.getInstance().getVersion()));
    }
}
