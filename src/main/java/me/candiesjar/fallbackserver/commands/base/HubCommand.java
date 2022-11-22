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

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class HubCommand implements SimpleCommand {
    private final FallbackServerVelocity fallbackServerVelocity;

    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();

        if (!(commandSource instanceof Player)) {
            VelocityMessages.PLAYER_ONLY.send(commandSource, new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        Player player = (Player) commandSource;
        Optional<ServerConnection> serverConnectionOptional = player.getCurrentServer();

        if (!serverConnectionOptional.isPresent()) {
            return;
        }

        ServerConnection serverConnection = serverConnectionOptional.get();
        String serverName = serverConnection.getServerInfo().getName();

        if (fallbackServerVelocity.isHub(serverName)) {
            VelocityMessages.ALREADY_IN_LOBBY.send(player, new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        LinkedList<RegisteredServer> lobbies = Lists.newLinkedList(fallbackServerVelocity.getFallingServerManager().getAll());

        if (lobbies.size() == 0) {
            VelocityMessages.NO_SERVER.send(player, new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        RegisteredServer server = lobbies.get(0);

        boolean isMaintenance = ServerUtils.isMaintenance(server);

        if (isMaintenance) {
            return;
        }

        player.createConnectionRequest(server).fireAndForget();

        VelocityMessages.MOVED_TO_HUB.send(player,
                new Placeholder("prefix", VelocityMessages.PREFIX.color()),
                new Placeholder("server", server.getServerInfo().getName())
        );

        if (VelocityMessages.USE_HUB_TITLE.get(Boolean.class)) {
            fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> TitleUtil.sendTitle(VelocityMessages.HUB_TITLE_FADE_IN.get(Integer.class),
                            VelocityMessages.HUB_TITLE_STAY.get(Integer.class),
                            VelocityMessages.HUB_TITLE_FADE_OUT.get(Integer.class),
                            ChatUtil.color(VelocityMessages.HUB_TITLE.get(String.class)),
                            ChatUtil.color(VelocityMessages.HUB_SUB_TITLE.get(String.class)),
                            player)).delay(VelocityMessages.HUB_TITLE_DELAY.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
        }
    }
}
