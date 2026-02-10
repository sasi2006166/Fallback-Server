package me.candiesjar.fallbackserver.commands.impl;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
import me.candiesjar.fallbackserver.reconnect.ReconnectManager;
import me.candiesjar.fallbackserver.reconnect.server.ReconnectSession;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class DebugCommand implements ISubCommand {

    private final FallbackServerBungee plugin;
    private final ReconnectManager reconnectManager;
    private final PlayerCacheManager playerCacheManager;

    public DebugCommand(FallbackServerBungee plugin) {
        this.plugin = plugin;
        this.reconnectManager = plugin.getReconnectManager();
        this.playerCacheManager = plugin.getPlayerCacheManager();
    }

    @Override
    public String getPermission() {
        return BungeeConfig.DEBUG_COMMAND_PERMISSION.getString();
    }

    @Override
    public boolean isEnabled() {
        return plugin.isDebug();
    }

    @Override
    public void perform(CommandSender sender, String[] arguments) {
        if (arguments.length < 2) {
            sender.sendMessage(new TextComponent("§cNo argument provided!"));
            return;
        }

        String command = arguments[1];

        switch (command.toLowerCase()) {
            case "help":
                sendHelp(sender);
                break;
            case "ping":
                handlePing(arguments, sender);
                break;
            case "check":
                checkPlayerReconnect(arguments, sender);
                break;
            case "file":
                handleFile(sender);
                break;
            default:
                sender.sendMessage(new TextComponent("§cUnknown argument!"));
                break;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(new TextComponent("§7--- §aFallbackServer Internals §7---"));
        sender.sendMessage(new TextComponent("§a/debug ping <server> §7- Ping a server"));
        sender.sendMessage(new TextComponent("§a/debug check <player|active> §7- Check a player's reconnect session or list active sessions"));
        sender.sendMessage(new TextComponent("§a/debug file §7- Generate diagnostics file"));
    }

    private void checkPlayerReconnect(String[] arguments, CommandSender sender) {
        if (checkArgumentsLength(sender, arguments, 3)) return;

        String name = arguments[2];

        if (name.equalsIgnoreCase("active")) {
            sender.sendMessage(new TextComponent("Active worker servers: " + reconnectManager.getWorkerMap().keySet()));
            sender.sendMessage(new TextComponent("Active queues: " + reconnectManager.getQueueMap().keySet()));
            return;
        }

        ProxiedPlayer player = plugin.getProxy().getPlayer(name);

        if (player == null) {
            sender.sendMessage(new TextComponent("§cPlayer not found!"));
            return;
        }

        UUID uuid = player.getUniqueId();
        ReconnectSession session = playerCacheManager.get(uuid);

        if (session == null) {
            sender.sendMessage(new TextComponent("§cPlayer is not in a reconnect session!"));
            return;
        }

        String serverName = session.getTargetServerInfo().getName();
        sender.sendMessage(new TextComponent("Server map size: " + reconnectManager.getQueueMap().get(serverName).getSize()));

        boolean activeWorker = reconnectManager.getWorkerMap().get(serverName).getPingTask() != null;
        sender.sendMessage(new TextComponent("Active worker: " + activeWorker));

        boolean activeTitleTask = session.getTitleTask() != null;
        sender.sendMessage(new TextComponent("Active title task: " + activeTitleTask));
    }

    private void handlePing(String[] arguments, CommandSender sender) {
        if (checkArgumentsLength(sender, arguments, 3)) return;

        String serverName = arguments[2];
        ServerInfo serverInfo = plugin.getProxy().getServerInfo(serverName);

        if (serverInfo == null) {
            sender.sendMessage(new TextComponent("§cServer not found!"));
            return;
        }

        serverInfo.ping((result, error) -> {
            if (error != null || result == null) {
                sender.sendMessage(new TextComponent("§cFailed to ping server!"));
                return;
            }

            sender.sendMessage(new TextComponent("§aPing successful!"));

            int players = result.getPlayers().getOnline();
            int max = result.getPlayers().getMax();

            sender.sendMessage(new TextComponent("§7Players: §a" + players + "§7/§c" + max));
        });
    }

    private void handleFile(CommandSender sender) {
        if (ErrorHandler.getDiagnostics().isEmpty()) {
            sender.sendMessage(new TextComponent("§cNo errors found! ;)"));
            return;
        }

        ErrorHandler.save();
        sender.sendMessage(new TextComponent("§aDiagnostics file created!"));
    }

    private boolean checkArgumentsLength(CommandSender sender, String[] arguments, int requiredLength) {
        if (arguments.length < requiredLength) {
            sender.sendMessage(new TextComponent("§cInsufficient arguments!"));
            return false;
        }
        return true;
    }
}
