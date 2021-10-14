package me.candiesjar.fallbackserver.bungee.commands;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.enums.ConfigFields;
import me.candiesjar.fallbackserver.bungee.enums.MessagesFields;
import me.candiesjar.fallbackserver.bungee.utils.RedirectServerWrapper;
import me.candiesjar.fallbackserver.bungee.utils.ServerGroup;
import me.candiesjar.fallbackserver.bungee.utils.TitleUtil;
import net.md_5.bungee.api.CommandSender;
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
            sender.sendMessage(new TextComponent(MessagesFields.NOT_PLAYER.getFormattedString()
                    .replace("%prefix%", MessagesFields.PREFIX.getFormattedString())));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (FallbackServerBungee.getInstance().isHub(player.getServer().getInfo())) {
            sender.sendMessage(new TextComponent(MessagesFields.ALREADY_IN_HUB.getFormattedString()
                    .replace("%prefix%", MessagesFields.PREFIX.getFormattedString())));
            return;
        }
        RedirectServerWrapper redirectServerWrapper = FallbackServerBungee.getInstance().getServer(player.getServer().getInfo().getName());
        ServerGroup serverGroup;
        if (redirectServerWrapper != null) {
            serverGroup = redirectServerWrapper.getServerGroup();
        } else serverGroup = FallbackServerBungee.getInstance().getUnknownServerGroup();
        if (serverGroup == null) {
            return;
        }
        RedirectServerWrapper targetServer = serverGroup.getRedirectServer(player, player.getServer().getInfo().getName(), true, serverGroup.getSpreadMode());
        if (targetServer == null) {
            return;
        }
        player.connect(targetServer.getServerInfo());
        if (MessagesFields.USE_HUB_TITLE.getBoolean()) {
            titleUtil.sendHubTitle(player);
        } else {
            player.sendMessage(new TextComponent(MessagesFields.CONNECT_TO_HUB.getFormattedString()
                    .replace("%prefix%", MessagesFields.PREFIX.getFormattedString())));
        }
    }
}
