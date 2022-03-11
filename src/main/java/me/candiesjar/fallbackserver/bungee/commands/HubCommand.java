package me.candiesjar.fallbackserver.bungee.commands;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.enums.BungeeConfig;
import me.candiesjar.fallbackserver.bungee.enums.BungeeMessages;
import me.candiesjar.fallbackserver.bungee.objects.FallingServer;
import me.candiesjar.fallbackserver.bungee.utils.TitleUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class HubCommand extends Command {

    private final TitleUtil titleUtil = new TitleUtil();

    public HubCommand() {
        super("", null, BungeeConfig.HUB_COMMANDS.getStringList().toArray(new String[0]));
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(BungeeMessages.ONLY_PLAYER.getFormattedString()
                    .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())));
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer) sender;
        if (FallbackServerBungee.getInstance().isHub(player.getServer().getInfo())) {
            player.sendMessage(new TextComponent(BungeeMessages.ALREADY_IN_HUB.getFormattedString()
                    .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())));
            return;
        }

        Map<ServerInfo, FallingServer> clonedMap = new HashMap<>(FallingServer.getServers());

        LinkedList<FallingServer> lobbies = new LinkedList<>(clonedMap.values());
        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        try {
            ServerInfo serverInfo = lobbies.get(0).getServerInfo();
            player.connect(serverInfo);
            if (BungeeMessages.USE_HUB_TITLE.getBoolean()) {
                titleUtil.sendHubTitle(player);
            } else {
                player.sendMessage(new TextComponent(BungeeMessages.CONNECT_TO_HUB.getFormattedString()
                        .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())));
            }
        } catch (IndexOutOfBoundsException ignored) {
            player.sendMessage(new TextComponent(BungeeMessages.NO_SERVER.getFormattedString()
                    .replace("%prefix%", BungeeMessages.PREFIX.getFormattedString())));
        }
    }
}
