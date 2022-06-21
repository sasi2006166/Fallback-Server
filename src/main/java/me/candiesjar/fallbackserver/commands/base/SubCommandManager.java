package me.candiesjar.fallbackserver.commands.base;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.commands.interfaces.SubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.ReloadSubCommand;
import me.candiesjar.fallbackserver.commands.subcommands.UpdateSubCommand;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.chat.ChatUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SubCommandManager extends Command implements TabExecutor {

    private final HashMap<String, SubCommand> subCommands = new HashMap<>();
    private static final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    public SubCommandManager() {
        super("fs");

        subCommands.put("reload", new ReloadSubCommand());
        subCommands.put("update", new UpdateSubCommand());

    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!BungeeConfig.COMMAND_WITHOUT_PERMISSION.getBoolean() && !sender.hasPermission(BungeeConfig.ADMIN_PERMISSION.getString())) {
            return;
        }

        if (!sender.hasPermission(BungeeConfig.ADMIN_PERMISSION.getString())) {
            sender.sendMessage(ChatUtil.asLegacyComponent("§8§l» §7Running §b§nFallback Server %version% §7by §b§nCandiesJar"
                    .replace("%version%", FallbackServerBungee.getInstance().getDescription().getVersion())));
            return;
        }

        if (args.length == 0) {
            BungeeMessages.MAIN_COMMAND.sendList(sender, new PlaceHolder("version", instance.getDescription().getVersion()));
            return;
        }

        if (!subCommands.containsKey(args[0].toLowerCase())) {
            BungeeMessages.PARAMETERS.send(sender, new PlaceHolder("prefix", FallbackServerBungee.getInstance().getPrefix()));
            return;
        }

        final SubCommand subCommand = subCommands.get(args[0].toLowerCase());

        if (!subCommand.isEnabled()) {
            return;
        }

        if (!sender.hasPermission(subCommand.getPermission())) {
            BungeeMessages.NO_PERMISSION.send(sender, new PlaceHolder("permission", subCommand.getPermission()), new PlaceHolder("prefix", FallbackServerBungee.getInstance().getPrefix()));
            return;
        }

        subCommand.perform(sender, args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(BungeeConfig.ADMIN_PERMISSION.getString())) {
            return Collections.emptyList();
        }

        if (BungeeConfig.TAB_COMPLETION.getBoolean()) {
            if (args.length == 1) {
                final List<String> completions = new ArrayList<>(subCommands.keySet());
                Collections.sort(completions);
                return completions;
            }
        }

        return Collections.emptyList();
    }
}
