package me.candiesjar.fallbackserver.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public class DebugSubCommand implements SubCommand {

    private final FallbackServerVelocity plugin;

    @Override
    public String getPermission() {
        return VelocityConfig.DEBUG_PERMISSION.get(String.class);
    }

    @Override
    public boolean isEnabled() {
        return plugin.isDebug();
    }

    @Override
    public void perform(CommandSource commandSource, String[] args) {

        if (args.length < 2) {
            commandSource.sendMessage(Component.text(ChatUtil.color("&cIncorrent arguments!")));
            return;
        }

        String command = args[1];

        switch (command.toLowerCase()) {
            case "ping":
                if (args.length < 3) {
                    commandSource.sendMessage(Component.text(ChatUtil.color("&cNo server provided!")));
                    return;
                }

                String serverName = args[2];

                commandSource.sendMessage(Component.text(ChatUtil.color("&cPinging server " + serverName + "...")));

                RegisteredServer serverInfo = plugin.getServer().getServer(serverName).get();

                serverInfo.ping().whenComplete((result, error) -> {
                    if (error != null || result == null) {
                        commandSource.sendMessage(Component.text(ChatUtil.color("&cError while pinging server!")));
                        return;
                    }

                    commandSource.sendMessage(Component.text(ChatUtil.color("&aServer pinged successfully!")));
                    commandSource.sendMessage(Component.text(ChatUtil.color("&aPlayers online: " + result.getPlayers().get())));
                    commandSource.sendMessage(Component.text(ChatUtil.color("&aVersion: " + result.getVersion().getName())));
                    commandSource.sendMessage(Component.text(ChatUtil.color("&aMax players: " + result.asBuilder().getMaximumPlayers())));
                });
        }

    }
}
