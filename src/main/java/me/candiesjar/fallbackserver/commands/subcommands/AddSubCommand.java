package me.candiesjar.fallbackserver.commands.subcommands;

import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.enums.BungeeServers;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

import java.util.List;

@RequiredArgsConstructor
public class AddSubCommand implements SubCommand {

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

        if (BungeeServers.SERVERS.getStringList().contains(server) || BungeeConfig.FALLBACK_LIST.getStringList().contains(server)) {
            BungeeMessages.SERVER_CONTAINED.send(sender, new Placeholder("server", server));
            return;
        }

        if (!ProxyServer.getInstance().getConfig().getServersCopy().containsKey(server)) {
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
