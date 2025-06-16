package me.candiesjar.fallbackserver.commands.impl;

import com.velocitypowered.api.command.CommandSource;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.config.VelocityMessages;
import me.candiesjar.fallbackserver.config.VelocityServers;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;

import java.util.List;

@RequiredArgsConstructor
public class RemoveCommand implements ISubCommand {

    private final FallbackServerVelocity plugin;

    @Override
    public String getPermission() {
        return VelocityConfig.REMOVE_COMMAND_PERMISSION.get(String.class);
    }

    @Override
    public boolean isEnabled() {
        return VelocityConfig.REMOVE_COMMAND.get(Boolean.class);
    }

    @Override
    public void perform(CommandSource sender, String[] args) {

        if (args.length < 2) {
            VelocityMessages.EMPTY_GROUP.send(sender, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        String server = args[1];

        if (!VelocityServers.SERVERS.getStringList().contains(server)) {
            VelocityMessages.SERVER_NOT_CONTAINED.send(sender, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)), new Placeholder("server", server));
            return;
        }

        remove(server);
        VelocityMessages.SERVER_REMOVED.send(sender, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)), new Placeholder("server", server));

    }

    private void remove(String serverName) {

        List<String> servers = VelocityServers.SERVERS.getStringList();

        servers.remove(serverName);

        if (servers.isEmpty()) {
            servers.add("fsplaceholder");
        }

        Utils.saveServers(servers);
        plugin.reloadAll();

        servers.clear();

    }
}
