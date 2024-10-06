package me.candiesjar.fallbackserver.commands.base;

import com.google.common.collect.Lists;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.managers.ServerManager;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HubCommand implements SimpleCommand {
    private final FallbackServerVelocity plugin;
    private final ServerTypeManager serverTypeManager;
    private final OnlineLobbiesManager onlineLobbiesManager;

    public HubCommand(FallbackServerVelocity plugin) {
        this.plugin = plugin;
        this.serverTypeManager = plugin.getServerTypeManager();
        this.onlineLobbiesManager = plugin.getOnlineLobbiesManager();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();

        if (!(commandSource instanceof Player)) {
            VelocityMessages.ONLY_PLAYER.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        Player player = (Player) commandSource;
        Optional<ServerConnection> optionalServerConnection = player.getCurrentServer();

        if (optionalServerConnection.isPresent() && isHub(optionalServerConnection.get())) {
            boolean useTitle = VelocityMessages.USE_ALREADY_IN_LOBBY_TITLE.get(Boolean.class);

            if (useTitle) {
                TitleUtil.sendTitle(VelocityMessages.ALREADY_IN_LOBBY_FADE_IN.get(Integer.class),
                        VelocityMessages.ALREADY_IN_LOBBY_STAY.get(Integer.class),
                        VelocityMessages.ALREADY_IN_LOBBY_FADE_OUT.get(Integer.class),
                        VelocityMessages.ALREADY_IN_LOBBY_TITLE.get(String.class),
                        VelocityMessages.ALREADY_IN_LOBBY_SUB_TITLE.get(String.class),
                        optionalServerConnection.get().getServer(),
                        player);
            }

            VelocityMessages.ALREADY_IN_LOBBY.send(player, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        String group = ServerManager.getGroupByName("default");
        List<RegisteredServer> lobbies = Lists.newArrayList(onlineLobbiesManager.get(group));
        lobbies.removeIf(Objects::isNull);
        lobbies.removeIf(server -> server.getServerInfo() == null);

        boolean hasMaintenance = plugin.isMaintenance();

        if (hasMaintenance) {
            lobbies.removeIf(ServerManager::checkMaintenance);
        }

        if (lobbies.isEmpty()) {
            VelocityMessages.NO_SERVER.send(player, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        lobbies.sort(Comparator.comparingInt(server -> server.getPlayersConnected().size()));
        RegisteredServer registeredServer = lobbies.get(0);

        VelocityMessages.MOVED_TO_HUB.send(player,
                new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                new Placeholder("server", registeredServer.getServerInfo().getName())
        );

        player.createConnectionRequest(registeredServer).fireAndForget();

        boolean useTitle = VelocityMessages.USE_HUB_TITLE.get(Boolean.class);

        if (useTitle) {
            plugin.getServer().getScheduler().buildTask(plugin, () ->
                            TitleUtil.sendTitle(VelocityMessages.HUB_TITLE_FADE_IN.get(Integer.class),
                                    VelocityMessages.HUB_TITLE_STAY.get(Integer.class),
                                    VelocityMessages.HUB_TITLE_FADE_OUT.get(Integer.class),
                                    VelocityMessages.HUB_TITLE.get(String.class),
                                    VelocityMessages.HUB_SUB_TITLE.get(String.class),
                                    registeredServer,
                                    player))
                    .delay(VelocityMessages.HUB_TITLE_DELAY.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
        }
    }

    private boolean isHub(ServerConnection serverConnection) {
        if (serverConnection == null) {
            return false;
        }

        RegisteredServer registeredServer = serverConnection.getServer();

        String group = ServerManager.getGroupByServer(registeredServer.getServerInfo().getName());

        if (group == null) {
            Utils.printDebug("The server " + registeredServer.getServerInfo().getName() + " does not exist!", true);
            Utils.printDebug("Seems that it isn't present inside the group list", true);
            Utils.printDebug("Please add it and run /fsv reload.", true);
            return false;
        }

        List<String> lobbies = serverTypeManager.getServerTypeMap().get(group).getLobbies();

        return lobbies.contains(registeredServer.getServerInfo().getName());
    }
}
