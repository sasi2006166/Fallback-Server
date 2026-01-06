package me.candiesjar.fallbackserver.commands.core;

import com.google.common.collect.Lists;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HubCommand extends Command {

    private final FallbackServerBungee plugin;
    private final ServerTypeManager serverTypeManager;
    private final OnlineLobbiesManager onlineLobbiesManager;

    public HubCommand(FallbackServerBungee plugin) {
        super(BungeeConfig.LOBBY_ALIASES.getStringList().get(0), null, BungeeConfig.LOBBY_ALIASES.getStringList().toArray(new String[0]));
        this.plugin = plugin;
        this.serverTypeManager = plugin.getServerTypeManager();
        this.onlineLobbiesManager = plugin.getOnlineLobbiesManager();
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            BungeeMessages.ONLY_PLAYER.send(sender);
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        ServerInfo playerServer = player.getServer().getInfo();

        if (isHub(playerServer)) {
            boolean useTitle = BungeeMessages.USE_ALREADY_IN_LOBBY_TITLE.getBoolean();

            if (useTitle) {
                plugin.getTitleUtil().sendTitle(BungeeMessages.ALREADY_IN_LOBBY_FADE_IN.getInt(),
                        BungeeMessages.ALREADY_IN_LOBBY_STAY.getInt(),
                        BungeeMessages.ALREADY_IN_LOBBY_FADE_OUT.getInt(),
                        BungeeMessages.ALREADY_IN_LOBBY_TITLE,
                        BungeeMessages.ALREADY_IN_LOBBY_SUB_TITLE,
                        playerServer,
                        player);
            }

            BungeeMessages.ALREADY_IN_LOBBY.send(player);
            return;
        }

        String group = ServerManager.getGroupByName("default");
        List<ServerInfo> lobbies = Lists.newArrayList(onlineLobbiesManager.get(group));
        lobbies.removeIf(Objects::isNull);

        boolean hasMaintenance = plugin.isMaintenance();

        if (hasMaintenance) {
            lobbies.removeIf(ServerManager::checkMaintenance);
        }

        if (lobbies.isEmpty()) {
            BungeeMessages.NO_SERVER.send(player);
            return;
        }

        lobbies.sort(Comparator.comparingInt(server -> server.getPlayers().size()));
        ServerInfo serverInfo = lobbies.get(0);

        player.connect(serverInfo);

        boolean useTitle = BungeeMessages.USE_HUB_TITLE.getBoolean();

        if (useTitle) {
            plugin.getTitleUtil().sendTitle(BungeeMessages.HUB_TITLE_FADE_IN.getInt(),
                    BungeeMessages.HUB_TITLE_STAY.getInt(),
                    BungeeMessages.HUB_TITLE_FADE_OUT.getInt(),
                    BungeeMessages.HUB_TITLE,
                    BungeeMessages.HUB_SUB_TITLE,
                    serverInfo,
                    player);
        }

        BungeeMessages.MOVED_TO_HUB.send(player, new Placeholder("server", serverInfo.getName()));

    }

    private boolean isHub(ServerInfo server) {
        String group = ServerManager.getGroupByServer(server.getName());

        if (group == null) {

            if (plugin.isDebug()) {
                Utils.printDebug("[HUB COMMAND] The server " + server.getName() + " does not exist!", true);
                Utils.printDebug("[HUB COMMAND] Please add it to default lobbies and run /fs reload.", true);
            }

            return false;
        }

        List<String> lobbies = serverTypeManager.getServerTypeMap().get(group).getLobbies();

        return lobbies.contains(server.getName());
    }
}