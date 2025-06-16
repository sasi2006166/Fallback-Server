package me.candiesjar.fallbackserver.commands.impl;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.handler.DebugLimboHandler;
import me.candiesjar.fallbackserver.handler.ErrorHandler;
import me.candiesjar.fallbackserver.utils.WorldUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public class DebugCommand implements ISubCommand {

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
            commandSource.sendMessage(Component.text(ChatUtil.formatColor("&cIncorrent arguments!")));
            return;
        }

        String command = args[1];

        switch (command) {
            case "spawn":
                handleSpawn(commandSource);
                break;
            case "file":
                handleFile(commandSource);
                break;
        }

        if (command.equalsIgnoreCase("ping")) {
            if (args.length < 3) {
                commandSource.sendMessage(Component.text(ChatUtil.formatColor("&cNo server provided!")));
                return;
            }

            String serverName = args[2];

            commandSource.sendMessage(Component.text(ChatUtil.formatColor("&cPinging server " + serverName + "...")));

            RegisteredServer serverInfo = plugin.getServer().getServer(serverName).get();

            serverInfo.ping().whenComplete((result, error) -> {
                if (error != null || result == null) {
                    commandSource.sendMessage(Component.text(ChatUtil.formatColor("&cError while pinging server!")));
                    return;
                }

                commandSource.sendMessage(Component.text(ChatUtil.formatColor("&aServer pinged successfully!")));
                commandSource.sendMessage(Component.text(ChatUtil.formatColor("&aPlayers online: " + result.getPlayers().get())));
                commandSource.sendMessage(Component.text(ChatUtil.formatColor("&aVersion: " + result.getVersion().getName())));
                commandSource.sendMessage(Component.text(ChatUtil.formatColor("&aMax players: " + result.asBuilder().getMaximumPlayers())));
            });
        }

    }

    private void handleSpawn(CommandSource commandSource) {
        Player player = (Player) commandSource;
        WorldUtil.getFallbackLimbo().spawnPlayer(player, new DebugLimboHandler());
    }

    private void handleFile(CommandSource commandSource) {
        if (ErrorHandler.getDiagnostics().isEmpty()) {
            commandSource.sendMessage(Component.text(ChatUtil.formatColor("&cNo errors found!")));
            return;
        }

        ErrorHandler.handle();
        commandSource.sendMessage(Component.text(ChatUtil.formatColor("&aDiagnostics file created!")));
    }
}
