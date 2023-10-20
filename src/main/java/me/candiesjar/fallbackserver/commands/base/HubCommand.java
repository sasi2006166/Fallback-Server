package me.candiesjar.fallbackserver.commands.base;

import com.google.common.collect.Lists;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.Placeholder;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import me.candiesjar.fallbackserver.utils.server.ServerUtils;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Comparator;
import java.util.List;

public class HubCommand extends Command {

    private final FallbackServerBungee fallbackServerBungee;

    public HubCommand(FallbackServerBungee fallbackServerBungee) {
        super(BungeeConfig.LOBBY_ALIASES.getStringList().get(0), null, BungeeConfig.LOBBY_ALIASES.getStringList().toArray(new String[0]));
        this.fallbackServerBungee = fallbackServerBungee;
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            BungeeMessages.ONLY_PLAYER.send(sender);
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        ServerInfo playerServer = player.getServer().getInfo();

        if (isHub(playerServer)) {
            BungeeMessages.ALREADY_IN_LOBBY.send(player);
            return;
        }

        List<FallingServer> lobbies = Lists.newArrayList();
        FallingServer.getServers().values().forEach(fallingServer -> {
            if (fallingServer != null && fallingServer.getServerInfo() != null) {
                lobbies.add(fallingServer);
            }
        });

        boolean hasMaintenance = fallbackServerBungee.isMaintenance();

        if (hasMaintenance) {
            lobbies.removeIf(fallingServer -> ServerUtils.checkMaintenance(fallingServer.getServerInfo()));
        }

        if (lobbies.isEmpty()) {
            BungeeMessages.NO_SERVER.send(player);
            return;
        }

        lobbies.sort(Comparator.comparingInt(server -> server.getServerInfo().getPlayers().size()));

        ServerInfo serverInfo = lobbies.get(0).getServerInfo();

        player.connect(serverInfo);

        BungeeMessages.MOVED_TO_HUB.send(player, new Placeholder("server", serverInfo.getName()));

        boolean useTitle = BungeeMessages.USE_HUB_TITLE.getBoolean();

        if (useTitle) {

            TitleUtil.sendTitle(BungeeMessages.HUB_TITLE_FADE_IN.getInt(),
                    BungeeMessages.HUB_TITLE_STAY.getInt(),
                    BungeeMessages.HUB_TITLE_FADE_OUT.getInt(),
                    BungeeMessages.HUB_TITLE,
                    BungeeMessages.HUB_SUB_TITLE,
                    serverInfo,
                    player);

        }

    }

    private boolean isHub(ServerInfo server) {
        return BungeeConfig.FALLBACK_LIST.getStringList().contains(server.getName());
    }
}
