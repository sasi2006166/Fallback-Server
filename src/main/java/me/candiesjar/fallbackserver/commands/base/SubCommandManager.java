package me.candiesjar.fallbackserver.commands.base;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.DebugSubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.LanguageSubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.ReloadSubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.UpdateSubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.utils.chat.ChatUtil;
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
        super("fs", null, "fallbackserverbungee");
        this.plugin = plugin;

        subCommands.put("reload", new ReloadSubCommand(plugin));
        subCommands.put("update", new UpdateSubCommand());
        subCommands.put("debug", new DebugSubCommand(plugin));
        subCommands.put("language", new LanguageSubCommand());

    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (BungeeConfig.HIDE_COMMAND.getBoolean() && !sender.hasPermission(BungeeConfig.ADMIN_PERMISSION.getString())) {
            return;
        }

        if (!sender.hasPermission(BungeeConfig.ADMIN_PERMISSION.getString())) {
            sender.sendMessage(ChatUtil.asLegacyComponent("§8§l» §7Running §b§nFallback Server %version% §7by §b§nCandiesJar"
                    .replace("%version%", plugin.getDescription().getVersion())));
            return;
        }

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
        if (!sender.hasPermission(BungeeConfig.ADMIN_PERMISSION.getString())) {
            return Collections.emptyList();
        }

        if (!BungeeConfig.TAB_COMPLETION.getBoolean()) {
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
