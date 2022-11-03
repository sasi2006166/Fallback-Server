package me.candiesjar.fallbackserver.commands.base;

import com.google.common.collect.Lists;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.objects.text.PlaceHolder;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import me.candiesjar.fallbackserver.utils.player.TitleUtil;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static me.candiesjar.fallbackserver.enums.VelocityMessages.*;

@RequiredArgsConstructor
public class HubCommand implements SimpleCommand {
    private final FallbackServerVelocity fallbackServerVelocity;

    @Override
    public void execute(Invocation invocation) {
        final CommandSource commandSource = invocation.source();

        if (!(commandSource instanceof Player)) {
            PLAYER_ONLY.send(commandSource, new PlaceHolder("prefix", PREFIX.color()));
            return;
        }

        final Player player = (Player) commandSource;
        final Optional<ServerConnection> serverConnectionOptional = player.getCurrentServer();

        if (!serverConnectionOptional.isPresent()) {
            return;
        }

        final ServerConnection serverConnection = serverConnectionOptional.get();
        final String serverName = serverConnection.getServerInfo().getName();

        if (fallbackServerVelocity.isHub(serverName)) {
            ALREADY_IN_LOBBY.send(player, new PlaceHolder("prefix", PREFIX.color()));
            return;
        }

        final LinkedList<RegisteredServer> lobbies = Lists.newLinkedList(fallbackServerVelocity.getFallingServerManager().getAll());

        if (lobbies.size() == 0) {
            NO_SERVER.send(player, new PlaceHolder("prefix", PREFIX.color()));
            return;
        }

        final RegisteredServer server = lobbies.get(0);

        player.createConnectionRequest(server).fireAndForget();

        MOVED_TO_HUB.send(player,
                new PlaceHolder("prefix", PREFIX.color()),
                new PlaceHolder("server", server.getServerInfo().getName())
        );

        if (USE_HUB_TITLE.get(Boolean.class)) {
            fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> TitleUtil.sendTitle(HUB_TITLE_FADE_IN.get(Integer.class),
                            HUB_TITLE_STAY.get(Integer.class),
                            HUB_TITLE_FADE_OUT.get(Integer.class),
                            ChatUtil.color(HUB_TITLE.get(String.class)),
                            ChatUtil.color(HUB_SUB_TITLE.get(String.class)),
                            player)).delay(HUB_TITLE_DELAY.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
        }
    }
}
