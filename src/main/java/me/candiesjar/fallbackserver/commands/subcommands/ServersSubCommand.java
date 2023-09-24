package me.candiesjar.fallbackserver.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.server.impl.FallingServerManager;
import me.candiesjar.fallbackserver.objects.text.Placeholder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ServersSubCommand implements SubCommand {

    private final FallbackServerVelocity plugin;
    private final FallingServerManager fallingServerManager;

    public ServersSubCommand(FallbackServerVelocity plugin) {
        this.plugin = plugin;
        this.fallingServerManager = plugin.getFallingServerManager();
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
        List<String> servers = VelocityConfig.LOBBIES_LIST.getStringList();
        AtomicReference<String> status = new AtomicReference<>(VelocityMessages.SERVERS_COMMAND_ONLINE.get(String.class));

        VelocityMessages.SERVERS_COMMAND_HEADER.sendList(sender);

        servers.forEach(server -> {
            Optional<RegisteredServer> registeredServerOptional = plugin.getServer().getServer(server);

            if (registeredServerOptional.isEmpty()) {
                return;
            }

            RegisteredServer registeredServer = registeredServerOptional.get();

            if (!fallingServerManager.getCache().containsValue(registeredServer)) {
                status.set(VelocityMessages.SERVERS_COMMAND_OFFLINE.get(String.class));
            }

            VelocityMessages.SERVERS_COMMAND_LIST.send(sender,
                    new Placeholder("server", registeredServer.getServerInfo().getName()),
                    new Placeholder("status", status.get()));
        });

        VelocityMessages.SERVERS_COMMAND_FOOTER.sendList(sender);

    }
}
