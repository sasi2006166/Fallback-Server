package me.candiesjar.fallbackserver.commands.subcommands;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.pastebin.builders.Pastebin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Collections;

public class DebugSubCommand implements SubCommand {

    private final FallbackServerBungee plugin;

    public DebugSubCommand(FallbackServerBungee plugin) {
        this.plugin = plugin;
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
            Utils.printDebug("§cNo arguments provided!", false);
            return;
        }

        String command = arguments[1];

        switch (command.toLowerCase()) {
            case "help":
                handleHelp(sender);
                break;
            case "ping":
                handlePing(arguments);
                break;
            case "file":
                handleFile(sender);
                break;
        }
    }

    private void handleHelp(CommandSender sender) {
        String devKey = "BPMf3G8q44u1PiJIEM_B4wrExp-Bhcss";
        StringBuilder builder = new StringBuilder();

        for (Plugin plugin : plugin.getProxy().getPluginManager().getPlugins()) {
            String name = plugin.getDescription().getName();

            if (name.startsWith("cmd") || name.startsWith("reconnect")) {
                continue;
            }

            builder.append(name).append(" ");
        }

        String proxyVersion = plugin.getProxy().getVersion();
        String pluginVersion = plugin.getDescription().getVersion();
        String name = plugin.getProxy().getName();

        builder.append("\n");
        builder.append("Proxy Version: ").append(proxyVersion).append("\n");
        builder.append("Proxy Name: ").append(name).append("\n");
        builder.append("Plugin Version: ").append(pluginVersion).append("\n");

        Pastebin pastebin = new Pastebin.PastebinBuilder()
                .setDevKey(devKey)
                .setText(Collections.singletonList(builder.toString()))
                .setName("Fallback Debug Info")
                .build();

        String response = pastebin.send();
        sender.sendMessage(new TextComponent("§a" + response));
    }

    private void handlePing(String[] arguments) {
        if (arguments.length < 3) {
            Utils.printDebug("§cNo server provided!", true);
            return;
        }

        String serverName = arguments[2];
        ServerInfo serverInfo = plugin.getProxy().getServerInfo(serverName);

        if (serverInfo == null) {
            Utils.printDebug("§cServer not found!", true);
            return;
        }

        serverInfo.ping((result, error) -> {
            if (error != null || result == null) {
                Utils.printDebug("§cError while pinging server!", true);
                return;
            }

            Utils.printDebug("§7Server pinged §asuccessfully!", false);

            int players = result.getPlayers().getOnline();
            int max = result.getPlayers().getMax();

            Utils.printDebug("§7Players: §a" + players + "§7/§c" + max, false);
        });
    }

    private void handleFile(CommandSender sender) {
        if (ErrorHandler.getSize() == 0) {
            sender.sendMessage(new TextComponent("§cNo errors found!"));
            return;
        }

        ErrorHandler.handle();
    }
}
