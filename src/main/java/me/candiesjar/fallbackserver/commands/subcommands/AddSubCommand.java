package me.candiesjar.fallbackserver.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.objects.ServerType;
import me.candiesjar.fallbackserver.utils.Utils;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.List;

@RequiredArgsConstructor
public class AddSubCommand implements SubCommand {

    private final FallbackServerVelocity plugin;

    @Override
    public String getPermission() {
        return VelocityConfig.ADD_COMMAND_PERMISSION.get(String.class);
    }

    @Override
    public boolean isEnabled() {
        return VelocityConfig.ADD_COMMAND.get(Boolean.class);
    }

    @Override
    public void perform(CommandSource commandSource, String[] args) {
        // /fs add <group> <server>
        if (args.length != 3) {
            VelocityMessages.EMPTY_GROUP.send(commandSource);
            return;
        }

        String group = args[1];
        String serverName = args[2];

        // check group exists
        if (!ServerManager.checkIfGroupExists(group)) {
            // send message
            return;
        }

        // check server exists
        RegisteredServer server = plugin.getServer().getServer(serverName).orElse(null);

        if (server == null) {
            // send message
            return;
        }

        ServerType serverType = plugin.getServerTypeManager().get(group);
        List<String> servers = serverType.getServers();

        if (servers.contains(serverName)) {
            // send message
            return;
        }

        addServer(group, serverName);
        Utils.printDebug("§7[§a+§7] Added server §a" + serverName + " §7to group §a" + group, true);
    }

    private void addServer(String group, String serverName) {
        ConfigurationSection section = plugin.getServersTextFile().getConfig().getConfigurationSection("servers." + group);
        List<String> servers = section.getStringList("servers");

        servers.add(serverName);
        section.set("servers", servers);

        plugin.getServersTextFile().save();
    }
}
