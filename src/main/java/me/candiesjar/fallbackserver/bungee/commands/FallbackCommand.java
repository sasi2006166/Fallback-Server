package me.candiesjar.fallbackserver.bungee.commands;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.enums.ConfigFields;
import me.candiesjar.fallbackserver.bungee.enums.MessagesFields;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FallbackCommand extends Command implements TabExecutor {

    public FallbackCommand() {
        super("fs");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!ConfigFields.COMMAND_WITHOUT_PERMISSION.getBoolean() && !sender.hasPermission(ConfigFields.PERMISSION.getString()))
            return;
        if (args.length > 0) {
            if (sender.hasPermission(ConfigFields.PERMISSION.getString())) {
                switch (args[0].toLowerCase()) {
                    case "reload":
                        FallbackServerBungee.getInstance().reloadConfig();
                        sender.sendMessage(new TextComponent(MessagesFields.RELOAD_MESSAGE.getFormattedString()
                                .replace("%prefix%", MessagesFields.PREFIX.getFormattedString())));
                        break;
                    case "version":
                        sender.sendMessage(new TextComponent("§8§l» §7Running §b§nFallback Server %version% §7by §b§nCandiesJar §8§l«"
                                .replace("%version%", FallbackServerBungee.getInstance().getDescription().getVersion())));
                        break;
                    default:
                        sender.sendMessage(new TextComponent(MessagesFields.PARAMETERS.getFormattedString()
                                .replace("%prefix%", MessagesFields.PREFIX.getFormattedString())));
                        break;
                }
            }
        } else if (sender.hasPermission(ConfigFields.PERMISSION.getString())) {
            for (String mainCommand : MessagesFields.MAIN_COMMAND.getStringList())
                sender.sendMessage(new TextComponent(ConfigFields.getFormattedString(mainCommand)
                        .replace("%version%", FallbackServerBungee.getInstance().getDescription().getVersion())));
        } else {
            sender.sendMessage(new TextComponent("§8§l» §7Running §b§nFallback Server %version% §7by §b§nCandiesJar §8§l«"
                    .replace("%version%", FallbackServerBungee.getInstance().getDescription().getVersion())));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (ConfigFields.TAB_COMPLETE.getBoolean())
            if (args.length == 1) {
                List<String> data = new ArrayList<>();
                data.add("reload");
                data.add("version");
                Collections.sort(data);
                return data;
            }
        return Collections.emptyList();
    }
}
