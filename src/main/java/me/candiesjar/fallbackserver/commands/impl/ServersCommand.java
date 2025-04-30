package me.candiesjar.fallbackserver.commands.impl;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ServersCommand implements ISubCommand {

    private final OnlineLobbiesManager onlineLobbiesManager;

    public ServersCommand(FallbackServerBungee plugin) {
        this.onlineLobbiesManager = plugin.getOnlineLobbiesManager();
    }

    @Override
    public String getPermission() {
        return BungeeConfig.SERVERS_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return BungeeConfig.SERVERS_COMMAND.getBoolean();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {
        HashMap<String, List<ServerInfo>> onlineLobbies = onlineLobbiesManager.getOnlineLobbies();

        if (onlineLobbies.isEmpty()) {
            return;
        }

        BungeeMessages.SERVERS_COMMAND_HEADER.sendList(sender);

        onlineLobbies.forEach((group, servers) -> {
            servers.removeIf(Objects::isNull);

            if (!servers.isEmpty()) {
                BungeeMessages.SERVERS_COMMAND_GROUP.send(sender, new Placeholder("group", group));

                servers.forEach(server -> {
                    String status = ServerManager.checkMaintenance(server) ? BungeeMessages.SERVERS_COMMAND_MAINTENANCE.getString() : BungeeMessages.SERVERS_COMMAND_ONLINE.getString();
                    BungeeMessages.SERVERS_COMMAND_LIST.send(sender,
                            new Placeholder("server", server.getName()),
                            new Placeholder("status", status));
                });
            }
        });

        BungeeMessages.SERVERS_COMMAND_FOOTER.sendList(sender);
    }

}
