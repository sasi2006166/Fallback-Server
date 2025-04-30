package me.candiesjar.fallbackserver.commands.impl;

import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.config.BungeeServers;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

import java.util.List;

@RequiredArgsConstructor
public class AddCommand implements ISubCommand {

    private final FallbackServerBungee plugin;

    @Override
    public String getPermission() {
        return BungeeConfig.ADD_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return BungeeConfig.ADD_COMMAND.getBoolean();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {
        if (arguments.length < 2) {
            BungeeMessages.EMPTY_SERVER.send(sender);
            return;
        }

        String server = arguments[1];

        if (BungeeServers.SERVERS.getStringList().contains(server) || BungeeConfig.FALLBACK_SECTION.getStringList().contains(server)) {
            BungeeMessages.SERVER_CONTAINED.send(sender, new Placeholder("server", server));
            return;
        }

        if (!ProxyServer.getInstance().getConfig().getServers().containsKey(server)) {
            BungeeMessages.UNAVAILABLE_SERVER.send(sender, new Placeholder("server", server));
            return;
        }

        save(server);
        BungeeMessages.SERVER_ADDED.send(sender, new Placeholder("server", server));
    }

    private void save(String serverName) {
        List<String> servers = BungeeServers.SERVERS.getStringList();

        servers.add(serverName);

        Utils.saveServers(servers);
        plugin.reloadTask();

        servers.clear();
    }
}
