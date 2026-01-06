package me.candiesjar.fallbackserver.commands.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.api.ISubCommand;
import me.candiesjar.fallbackserver.commands.impl.DebugCommand;
import me.candiesjar.fallbackserver.commands.impl.ReloadCommand;
import me.candiesjar.fallbackserver.commands.impl.ServersCommand;
import me.candiesjar.fallbackserver.commands.impl.StatusCommand;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SubCommandManager extends Command implements TabExecutor {

    private final FallbackServerBungee plugin;
    private final HashMap<String, ISubCommand> subCommands = Maps.newHashMap();

    public SubCommandManager(FallbackServerBungee plugin) {
        super("fs", BungeeConfig.ADMIN_PERMISSION.getString(), "fallbackserverbungee");
        this.plugin = plugin;

        setPermissionMessage("§8§l» §7Running §b§nFallback Server §b" + plugin.getVersion() + " §7by §b§nCandiesJar §8§l«");

        subCommands.put("debug", new DebugCommand(plugin));
        subCommands.put("reload", new ReloadCommand(plugin));
        subCommands.put("status", new StatusCommand(plugin));
        subCommands.put("servers", new ServersCommand(plugin));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            BungeeMessages.MAIN_COMMAND.sendList(sender, new Placeholder("version", plugin.getDescription().getVersion()));
            return;
        }

        if (!subCommands.containsKey(args[0].toLowerCase())) {
            BungeeMessages.CORRECT_SYNTAX.send(sender);
            return;
        }

        ISubCommand ISubCommand = subCommands.get(args[0].toLowerCase());

        if (!ISubCommand.isEnabled()) {
            return;
        }

        if (!sender.hasPermission(ISubCommand.getPermission())) {
            BungeeMessages.NO_PERMISSION.send(sender, new Placeholder("permission", ISubCommand.getPermission()));
            return;
        }

        ISubCommand.perform(sender, args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!BungeeConfig.TAB_COMPLETE.getBoolean()) {
            return Collections.emptyList();
        }

        String permission = BungeeConfig.ADMIN_PERMISSION.getString();

        if (!sender.hasPermission(permission)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = Lists.newArrayList(subCommands.keySet());
            Collections.sort(completions);
            return completions;
        }

        return Collections.emptyList();
    }
}
