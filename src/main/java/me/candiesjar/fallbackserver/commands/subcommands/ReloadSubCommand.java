package me.candiesjar.fallbackserver.commands.subcommands;

import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.base.HubCommand;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.TextFile;
import net.md_5.bungee.api.CommandSender;

@RequiredArgsConstructor
public class ReloadSubCommand implements SubCommand {

    private final FallbackServerBungee plugin;

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
                plugin.getProxy().getPluginManager().registerCommand(plugin, new HubCommand(plugin));
            } else {
                plugin.getProxy().getPluginManager().unregisterCommand(new HubCommand(plugin));
            }

        }

        plugin.reloadTask();

        BungeeMessages.RELOAD.send(sender);
    }
}
