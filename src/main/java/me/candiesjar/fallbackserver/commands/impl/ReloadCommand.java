package me.candiesjar.fallbackserver.commands.impl;

import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.core.HubCommand;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
import me.candiesjar.fallbackserver.objects.text.TextFile;
import me.candiesjar.fallbackserver.utils.ReconnectUtil;
import me.candiesjar.fallbackserver.utils.tasks.PingTask;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;

@RequiredArgsConstructor
public class ReloadCommand implements ISubCommand {

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
        boolean wasCommandEnabled = BungeeConfig.LOBBY_COMMAND.getBoolean();

        PingTask.getTask().cancel();
        TextFile.reloadAll();

        boolean isCommandEnabled = BungeeConfig.LOBBY_COMMAND.getBoolean();

        if (wasCommandEnabled != isCommandEnabled) {
            HubCommand hubCommand = new HubCommand(plugin);

            ErrorHandler.add(Severity.INFO, "Hub command status has changed to " + isCommandEnabled);

            if (isCommandEnabled) {
                plugin.getProxy().getPluginManager().registerCommand(plugin, hubCommand);
            } else {
                plugin.getProxy().getPluginManager().unregisterCommand(hubCommand);
            }
        }

        ServerInfo reconnectServer = ReconnectUtil.checkForPhysicalServer();
        plugin.setReconnectServer(reconnectServer);

        plugin.getServerTypeManager().clear();
        plugin.getOnlineLobbiesManager().clear();
        ErrorHandler.getDiagnostics().clear();

        plugin.loadServers();
        plugin.reloadTask();

        BungeeMessages.RELOAD.send(sender);
    }
}
