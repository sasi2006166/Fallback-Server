package me.candiesjar.fallbackserver.bungee.commands;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.enums.ConfigFields;
import me.candiesjar.fallbackserver.bungee.enums.MessagesFields;
import me.candiesjar.fallbackserver.bungee.utils.TitleUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HubCommand extends Command {

    private final TitleUtil titleUtil = new TitleUtil();

    public HubCommand() {
        super("", null, ConfigFields.HUB_COMMANDS.getStringList().toArray(new String[0]));
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(MessagesFields.ONLY_PLAYER.getFormattedString()
                    .replace("%prefix%", MessagesFields.PREFIX.getFormattedString())));
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer) sender;
        if (FallbackServerBungee.getInstance().isHub(player.getServer().getInfo())) {
            player.sendMessage(new TextComponent(MessagesFields.ALREADY_IN_HUB.getFormattedString()
                    .replace("%prefix%", MessagesFields.PREFIX.getFormattedString())));
            return;
        }
        player.connect(ServerConnectRequest.builder().build()); // TODO FIX
        if (MessagesFields.USE_HUB_TITLE.getBoolean()) {
            titleUtil.sendHubTitle(player);
        } else {
            player.sendMessage(new TextComponent(MessagesFields.CONNECT_TO_HUB.getFormattedString()
                    .replace("%prefix%", MessagesFields.PREFIX.getFormattedString())));
        }
    }
}
