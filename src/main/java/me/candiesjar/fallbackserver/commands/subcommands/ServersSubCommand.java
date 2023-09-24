package me.candiesjar.fallbackserver.commands.subcommands;

import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.Placeholder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class ServersSubCommand implements SubCommand {

    private final FallbackServerBungee plugin;

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
        List<String> servers = BungeeConfig.FALLBACK_LIST.getStringList();
        AtomicReference<String> status = new AtomicReference<>(BungeeMessages.SERVERS_COMMAND_ONLINE.getString());

        BungeeMessages.SERVERS_COMMAND_HEADER.sendList(sender);

        servers.forEach(server -> {
            ServerInfo serverInfo = plugin.getProxy().getServerInfo(server);

            if (serverInfo == null) {
                return;
            }

            if (!FallingServer.getServers().containsKey(serverInfo)) {
                status.set(BungeeMessages.SERVERS_COMMAND_OFFLINE.getString());
            }

            BungeeMessages.SERVERS_COMMAND_LIST.send(sender,
                    new Placeholder("server", serverInfo.getName()),
                    new Placeholder("status", status.get()));
        });

        BungeeMessages.SERVERS_COMMAND_FOOTER.sendList(sender);

    }
}
