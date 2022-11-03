package me.candiesjar.fallbackserver.commands.base;

import com.google.common.collect.Lists;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.api.HubAPI;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.utils.TitleUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Comparator;
import java.util.LinkedList;

import static me.candiesjar.fallbackserver.enums.BungeeMessages.*;

public class HubCommand extends Command {

    private static final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    public HubCommand() {
        super(BungeeConfig.LOBBY_ALIASES.getStringList().get(0), null, BungeeConfig.LOBBY_ALIASES.getStringList().toArray(new String[0]));
    }

    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof ProxiedPlayer)) {
            PLAYER_ONLY.send(sender, new Placeholder("prefix", instance.getPrefix()));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        ServerInfo playerServer = player.getServer().getInfo();

        if (FallbackServerBungee.getInstance().isHub(playerServer)) {
            ALREADY_IN_LOBBY.send(player, new Placeholder("prefix", instance.getPrefix()));
            return;
        }

        LinkedList<FallingServer> lobbies = Lists.newLinkedList(FallingServer.getServers().values());

        if (lobbies.size() == 0) {
            NO_SERVER.send(player, new Placeholder("prefix", instance.getPrefix()));
            return;
        }

        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        ServerInfo serverInfo = lobbies.get(0).getServerInfo();

        player.connect(serverInfo);

        MOVED_TO_HUB.send(player, new Placeholder("prefix", instance.getPrefix()), new Placeholder("server", serverInfo.getName()));

        boolean hubTitle = USE_HUB_TITLE.getBoolean();

        if (hubTitle) {

            TitleUtil.sendTitle(HUB_TITLE_FADE_IN.getInt(),
                    HUB_TITLE_STAY.getInt(),
                    HUB_TITLE_FADE_OUT.getInt(),
                    HUB_TITLE,
                    HUB_SUB_TITLE,
                    serverInfo,
                    player);

        }

        ProxyServer.getInstance().getScheduler().runAsync(instance, () -> instance.getProxy().getPluginManager().callEvent(new HubAPI(player, playerServer, serverInfo)));

    }
}
