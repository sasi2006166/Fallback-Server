package me.candiesjar.fallbackserveraddon.commands;

import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class FSACommand implements CommandExecutor, TabCompleter {

    private final FallbackServerAddon plugin = FallbackServerAddon.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        if (strings.length != 1 || !strings[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(Utils.color("&8&l» &7Running &b&nFallback Server Addon version &7by &b&nCandiesJar")
                    .replace("version", plugin.getDescription().getVersion()));
            return true;
        }

        if (!sender.hasPermission(plugin.getConfig().getString("settings.reload_permission", "fallbackserveradmin.reload"))) {
            sender.sendMessage(Utils.color("&8&l» &7Running &b&nFallback Server Addon version &7by &b&nCandiesJar")
                    .replace("version", plugin.getDescription().getVersion()));
            return true;
        }

        String oldValue = plugin.getConfig().getString("settings.mode", "NONE");
        Utils.reloadConfig(sender);
        plugin.executeReload(oldValue);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] strings) {
        final List<String> completions = new ArrayList<>();
        completions.add("reload");
        return completions;
    }
}
