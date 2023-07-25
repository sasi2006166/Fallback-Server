package me.candiesjar.fallbackserver.commands.base;

import com.google.common.collect.Lists;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.ServerUtils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class HubCommand implements SimpleCommand {
    private final FallbackServerVelocity plugin;

    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();

        if (!(commandSource instanceof Player)) {
            VelocityMessages.PLAYER_ONLY.send(commandSource, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        Player player = (Player) commandSource;

        Optional<ServerConnection> serverConnectionOptional = player.getCurrentServer();

        if (serverConnectionOptional.isEmpty()) {
            return;
        }

        ServerConnection serverConnection = serverConnectionOptional.get();
        String serverName = serverConnection.getServerInfo().getName();

        if (plugin.isHub(serverName)) {
            VelocityMessages.ALREADY_IN_LOBBY.send(player, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        List<RegisteredServer> lobbies = Lists.newArrayList(plugin.getFallingServerManager().getAll());

        if (lobbies.isEmpty()) {
            VelocityMessages.NO_SERVER.send(player, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            return;
        }

        boolean useMaintenance = plugin.isMaintenance();

        if (useMaintenance) {
            lobbies.removeIf(ServerUtils::isMaintenance);
        }

        lobbies.sort(Comparator.comparingInt(o -> o.getPlayersConnected().size()));

        RegisteredServer registeredServer = lobbies.get(0);

        lobbies.remove(registeredServer);

        player.createConnectionRequest(registeredServer).fireAndForget();

        VelocityMessages.MOVED_TO_HUB.send(player,
                new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)),
                new Placeholder("server", registeredServer.getServerInfo().getName())
        );

        if (VelocityMessages.USE_HUB_TITLE.get(Boolean.class)) {
            plugin.getServer().getScheduler().buildTask(plugin, () -> TitleUtil.sendTitle(VelocityMessages.HUB_TITLE_FADE_IN.get(Integer.class),
                            VelocityMessages.HUB_TITLE_STAY.get(Integer.class),
                            VelocityMessages.HUB_TITLE_FADE_OUT.get(Integer.class),
                            ChatUtil.color(VelocityMessages.HUB_TITLE.get(String.class)),
                            ChatUtil.color(VelocityMessages.HUB_SUB_TITLE.get(String.class)),
                            player)).delay(VelocityMessages.HUB_TITLE_DELAY.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
        }
    }
}
