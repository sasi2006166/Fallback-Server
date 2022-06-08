package me.candiesjar.fallbackserver.commands.base;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.api.HubAPI;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.TitleUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
            PLAYER_ONLY.send(sender, new PlaceHolder("prefix", instance.getPrefix()));
            return;
        }

        final ProxiedPlayer player = (ProxiedPlayer) sender;
        final ServerInfo playerServer = player.getServer().getInfo();

        if (FallbackServerBungee.getInstance().isHub(player.getServer().getInfo())) {
            ALREADY_IN_LOBBY.send(player, new PlaceHolder("prefix", instance.getPrefix()));
            return;
        }

        final Map<ServerInfo, FallingServer> clonedMap = new HashMap<>(FallingServer.getServers());

        final LinkedList<FallingServer> lobbies = new LinkedList<>(clonedMap.values());

        if (lobbies.size() == 0) {
            NO_SERVER.send(player, new PlaceHolder("prefix", instance.getPrefix()));
            return;
        }

        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());


        final ServerInfo serverInfo = lobbies.get(0).getServerInfo();

        player.connect(serverInfo);

        MOVED_TO_HUB.send(player, new PlaceHolder("prefix", instance.getPrefix()), new PlaceHolder("server", serverInfo.getName()));

        if (USE_HUB_TITLE.getBoolean()) {

            titleUtil.sendTitle(HUB_TITLE_FADE_IN.getInt(),
                    HUB_TITLE_STAY.getInt(),
                    HUB_TITLE_FADE_OUT.getInt(),
                    HUB_TITLE,
                    HUB_SUB_TITLE,
                    player);

        }

        ProxyServer.getInstance().getScheduler().runAsync(instance, () -> instance.getProxy().getPluginManager().callEvent(new HubAPI(player, playerServer, serverInfo)));

    }
}
