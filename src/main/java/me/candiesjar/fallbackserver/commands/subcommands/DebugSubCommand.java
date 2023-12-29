package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;

public class DebugSubCommand implements SubCommand {

    private final FallbackServerBungee plugin;

    public DebugSubCommand(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPermission() {
        return BungeeConfig.DEBUG_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return plugin.isDebug();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {

        if (arguments.length < 2) {
            Utils.printDebug("§cNo arguments provided!", false);
            return;
        }

        String command = arguments[1];

        switch (command.toLowerCase()) {
            case "ping":

                if (arguments.length < 3) {
                    Utils.printDebug("§cNo server provided!", true);
                    return;
                }

                String serverName = arguments[2];
                ServerInfo serverInfo = plugin.getProxy().getServerInfo(serverName);

                if (serverInfo == null) {
                    Utils.printDebug("§cServer not found!", false);
                    return;
                }

                Utils.printDebug("§cPinging server " + serverName + "...", false);

                serverInfo.ping((result, error) -> {
                    if (error != null || result == null) {
                        Utils.printDebug("§cError while pinging server!", false);
                        return;
                    }

                    Utils.printDebug("§cServer pinged successfully!", false);

                    int players = result.getPlayers().getOnline();

                    Utils.printDebug("§cPlayers: " + players, false);

                    int max = result.getPlayers().getMax();

                    Utils.printDebug("§cPlayers: " + players + "/" + max, false);
                });
                break;
        }

    }
}
