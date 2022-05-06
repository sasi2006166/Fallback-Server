package me.candiesjar.fallbackserver.commands.base;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.TitleUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static me.candiesjar.fallbackserver.enums.BungeeMessages.*;

public class HubCommand extends Command {

    private final TitleUtil titleUtil = new TitleUtil();
    private static final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    public HubCommand() {
        super("", null, BungeeConfig.LOBBY_ALIASES.getStringList().toArray(new String[0]));
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            ONLY_PLAYER.send(sender, new PlaceHolder("prefix", instance.getPrefix()));
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) sender;

        if (FallbackServerBungee.getInstance().isHub(player.getServer().getInfo())) {
            ALREADY_IN_HUB.send(sender, new PlaceHolder("prefix", instance.getPrefix()));
            return;
        }

        Map<ServerInfo, FallingServer> clonedMap = new HashMap<>(FallingServer.getServers());

        LinkedList<FallingServer> lobbies = new LinkedList<>(clonedMap.values());
        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        if (lobbies.size() == 0) {
            NO_SERVER.send(sender, new PlaceHolder("prefix", instance.getPrefix()));
            return;
        }

        ServerInfo serverInfo = lobbies.get(0).getServerInfo();
        player.connect(serverInfo);

        if (USE_HUB_TITLE.getBoolean()) {
            titleUtil.sendHubTitle(player);
        } else {
            CONNECT_TO_HUB.send(sender, new PlaceHolder("prefix", instance.getPrefix()));
        }

    }
}