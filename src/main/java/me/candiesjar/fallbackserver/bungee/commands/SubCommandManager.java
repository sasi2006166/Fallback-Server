package me.candiesjar.fallbackserver.bungee.commands;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.commands.subCommands.ReloadSubCommand;
import me.candiesjar.fallbackserver.bungee.enums.BungeeConfig;
import me.candiesjar.fallbackserver.bungee.enums.BungeeMessages;
import me.candiesjar.fallbackserver.bungee.enums.SubCommandType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SubCommandManager extends Command implements TabExecutor {

    private final HashMap<String, SubCommand> subCommands = new HashMap<>();

    public SubCommandManager() {
        super("fs");

        subCommands.put("reload", new ReloadSubCommand());

    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!BungeeConfig.COMMAND_WITHOUT_PERMISSION.getBoolean() && !sender.hasPermission(BungeeConfig.PERMISSION.getString()))
            return;

        if (!sender.hasPermission(BungeeConfig.PERMISSION.getString())) {
            sender.sendMessage(new TextComponent("§8§l» §7Running §b§nFallback Server %version% §7by §b§nCandiesJar"
                    .replace("%version%", FallbackServerBungee.getInstance().getDescription().getVersion())));
            return;
        }

        if (args.length == 0) {
            for (String mainCommand : BungeeMessages.MAIN_COMMAND.getStringList())
                sender.sendMessage(new TextComponent(BungeeMessages.getFormattedString(mainCommand)
                        .replace("%version%", FallbackServerBungee.getInstance().getDescription().getVersion())));
            return;
        }

        if (!subCommands.containsKey(args[0].toLowerCase())) {
            sender.sendMessage(new TextComponent(BungeeMessages.PARAMETERS.getFormattedString()
                    .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())));
            return;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());

        if (!sender.hasPermission(subCommand.getPermission())) {
            BungeeMessages.NO_PERMISSION.getFormattedString()
                    .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString()
                            .replace("%permission%", subCommand.getPermission()));
            return;
        }

        if (subCommand.getType() == SubCommandType.ONLY_PLAYER && !(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(BungeeMessages.ONLY_PLAYER.getFormattedString()
                    .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())));
            return;
        }
        subCommand.perform(sender, args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(BungeeConfig.PERMISSION.getString()))
            return null;
        if (BungeeConfig.TAB_COMPLETE.getBoolean())
            if (args.length == 1) {
                List<String> data = new ArrayList<>();
                data.add("reload");
                data.add("add");
                data.add("set");
                Collections.sort(data);
                return data;
            }
        return Collections.emptyList();
    }
}
