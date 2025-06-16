package me.candiesjar.fallbackserver.commands.impl;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.config.VelocityMessages;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.objects.text.Placeholder;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ServersCommand implements ISubCommand {

    private final OnlineLobbiesManager onlineLobbiesManager;

    public ServersCommand(FallbackServerVelocity plugin) {
        this.onlineLobbiesManager = plugin.getOnlineLobbiesManager();
    }

    @Override
    public String getPermission() {
        return VelocityConfig.SERVERS_COMMAND_PERMISSION.get(String.class);
    }

    @Override
    public boolean isEnabled() {
        return VelocityConfig.SERVERS_COMMAND.get(Boolean.class);
    }

    @Override
    public void perform(CommandSource sender, String[] args) {
        HashMap<String, List<RegisteredServer>> onlineLobbies = onlineLobbiesManager.getOnlineLobbies();

        if (onlineLobbies.isEmpty()) {
            return;
        }

        VelocityMessages.SERVERS_COMMAND_HEADER.sendList(sender);

        onlineLobbies.forEach((group, servers) -> {
            servers.removeIf(Objects::isNull);

            if (!servers.isEmpty()) {
                VelocityMessages.SERVERS_COMMAND_GROUP.send(sender, new Placeholder("group", group));

                servers.forEach(server -> {
                    String status = ServerManager.checkMaintenance(server) ? VelocityMessages.SERVERS_COMMAND_MAINTENANCE.get(String.class) : VelocityMessages.SERVERS_COMMAND_ONLINE.get(String.class);
                    VelocityMessages.SERVERS_COMMAND_LIST.send(sender,
                            new Placeholder("server", server.getServerInfo().getName()),
                            new Placeholder("status", status));
                });
            }
        });

        VelocityMessages.SERVERS_COMMAND_FOOTER.sendList(sender);
    }
}
