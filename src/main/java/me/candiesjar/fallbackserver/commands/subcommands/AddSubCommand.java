package me.candiesjar.fallbackserver.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.enums.VelocityServers;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;

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

        if (args.length < 2) {
            VelocityMessages.EMPTY_SERVER.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        String server = args[1];

        if (VelocityServers.SERVERS.getStringList().contains(server) || VelocityConfig.LOBBIES_LIST.getStringList().contains(server)) {
            VelocityMessages.SERVER_CONTAINED.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)), new Placeholder("server", server));
            return;
        }

        if (!plugin.getServer().getConfiguration().getServers().containsKey(server)) {
            VelocityMessages.UNAVAILABLE_SERVER.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)), new Placeholder("server", server));
            return;
        }

        save(server);
        VelocityMessages.SERVER_ADDED.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)), new Placeholder("server", server));

    }

    private void save(String serverName) {

        List<String> servers = VelocityServers.SERVERS.getStringList();

        servers.add(serverName);

        Utils.saveServers(servers);
        plugin.reloadAll();

        servers.clear();

    }
}
