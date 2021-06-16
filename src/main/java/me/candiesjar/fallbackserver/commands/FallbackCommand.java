package me.candiesjar.fallbackserver.commands;

import me.candiesjar.fallbackserver.FallbackServer;
import me.candiesjar.fallbackserver.utils.Fields;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class FallbackCommand extends Command {

    public FallbackCommand() {
        super("fs");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (sender.hasPermission(Fields.PERMISSION.getString())) {
                switch (args[0]) {
                    case "reload":
                        FallbackServer.instance.reloadConfig();
                        sender.sendMessage(new TextComponent(Fields.RELOADMESSAGE.getFormattedString()));
                        break;
                    case "version":
                        sender.sendMessage(new TextComponent("§7Server is using §bFallbackServer %version% §7by §bCandiesJar"
                                .replace("%version%", FallbackServer.instance.getDescription().getVersion())));
                        break;
                    default:
                        sender.sendMessage(new TextComponent(Fields.CORRECTSYNTAX.getFormattedString()));
                        break;
                }
            } else {
                sender.sendMessage(new TextComponent(Fields.NOPERMISSION.getFormattedString()
                        .replace("%permission%", Fields.PERMISSION.getString())));
            }
        } else if (sender.hasPermission(Fields.PERMISSION.getString())) {
            for (String mainCommand : Fields.MAINCOMMAND.getStringList()) {
                sender.sendMessage(new TextComponent(Fields.getFormattedString(mainCommand)));
            }
        } else {
            sender.sendMessage(new TextComponent("§7Server is using §bFallbackServer %version% §7by §bCandiesJar"
                    .replace("%version%", FallbackServer.instance.getDescription().getVersion())));
        }
    }
}
