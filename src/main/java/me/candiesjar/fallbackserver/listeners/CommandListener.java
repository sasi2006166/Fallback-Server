package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.config.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CommandListener {
    private final FallbackServerVelocity fallbackServerVelocity;

    @Subscribe
    public void onPlayerChat(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) {
            return;
        }

        Optional<ServerConnection> serverConnectionOptional = player.getCurrentServer();

        if (serverConnectionOptional.isEmpty()) {
            return;
        }

        ServerConnection serverConnection = serverConnectionOptional.get();
        String serverName = serverConnection.getServerInfo().getName();
        String command = event.getCommand();

        if (player.hasPermission(VelocityConfig.ADMIN_PERMISSION.get(String.class))) {
            return;
        }

        if (!command.isEmpty()) {
            String[] args = command.split(" ");
            command = args[0];
        }

        List<String> blockedCommands = fallbackServerVelocity.getConfigTextFile().getConfig().getStringList("settings.command_blocker_list." + serverName);
        boolean isBlacklistedCommand = ChatUtil.checkMessage(command, blockedCommands);

        if (isBlacklistedCommand) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            VelocityMessages.BLOCKED_COMMAND.send(player, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
        }
    }
}
