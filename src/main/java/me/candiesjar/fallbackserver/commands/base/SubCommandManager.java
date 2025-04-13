package me.candiesjar.fallbackserver.commands.base;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.DebugSubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.ReloadSubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.ServersSubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.StatusSubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SubCommandManager extends Command implements TabExecutor {

    private final FallbackServerBungee plugin;
    private final HashMap<String, SubCommand> subCommands = Maps.newHashMap();

    public SubCommandManager(FallbackServerBungee plugin) {
        super("fs", BungeeConfig.ADMIN_PERMISSION.getString(), "fallbackserverbungee");
        this.plugin = plugin;

        setPermissionMessage("§8§l» §7Running §b§nFallback Server §b" + plugin.getVersion() + " §7by §b§nCandiesJar §8§l«");

        subCommands.put("debug", new DebugSubCommand(plugin));
        subCommands.put("reload", new ReloadSubCommand(plugin));
        subCommands.put("status", new StatusSubCommand(plugin));
        subCommands.put("servers", new ServersSubCommand(plugin));
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

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());

        if (!subCommand.isEnabled()) {
            return;
        }

        if (!sender.hasPermission(subCommand.getPermission())) {
            BungeeMessages.NO_PERMISSION.send(sender, new Placeholder("permission", subCommand.getPermission()));
            return;
        }

        subCommand.perform(sender, args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        boolean tabComplete = BungeeConfig.TAB_COMPLETE.getBoolean();

        if (!tabComplete) {
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
