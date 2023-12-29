package me.candiesjar.fallbackserveraddon.commands;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.utils.ChatUtil;
import me.candiesjar.fallbackserveraddon.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FSACommand implements CommandExecutor {

    private final FallbackServerAddon plugin;

    public FSACommand(FallbackServerAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatUtil.color("&8&l» &7Running &b&nFallback Server Addon version &7by &b&nCandiesJar")
                    .replace("version", plugin.getDescription().getVersion()));
            return true;
        }

        if (!args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatUtil.color("&8&l» &7Running &b&nFallback Server Addon version &7by &b&nCandiesJar")
                    .replace("version", plugin.getDescription().getVersion()));
            return true;
        }

        if (!sender.hasPermission(plugin.getConfig().getString("settings.reload_permission", "fallbackserveradmin.reload"))) {
            sender.sendMessage(ChatUtil.color("&8&l» &7Running &b&nFallback Server Addon version &7by &b&nCandiesJar")
                    .replace("version", plugin.getDescription().getVersion()));
            return true;
        }

        String oldValue = plugin.getConfig().getString("settings.mode", "NONE");
        Utils.reloadConfig();
        plugin.executeReload(oldValue);

        return true;
    }
}
