package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.TextFile;
import me.candiesjar.fallbackserver.utils.tasks.PingTask;
import net.md_5.bungee.api.CommandSender;

public class ReloadSubCommand implements SubCommand {

    private final FallbackServerBungee fallbackServerBungee;

    public ReloadSubCommand(FallbackServerBungee fallbackServerBungee) {
        this.fallbackServerBungee = fallbackServerBungee;
    }

    @Override
    public String getPermission() {
        return BungeeConfig.RELOAD_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {

        boolean hubReload = BungeeConfig.LOBBY_COMMAND.getBoolean();

        TextFile.reloadAll();

        boolean reloadCommand = BungeeConfig.LOBBY_COMMAND.getBoolean();

        if (hubReload != reloadCommand) {

            if (reloadCommand) {
                fallbackServerBungee.getProxy().getPluginManager().registerCommand(fallbackServerBungee, new HubCommand(fallbackServerBungee));
            } else {
                fallbackServerBungee.getProxy().getPluginManager().unregisterCommand(new HubCommand(fallbackServerBungee));
            }

        }

        PingTask.getTask().cancel();
        PingTask.start();

        BungeeMessages.RELOAD.send(sender);
    }
}
