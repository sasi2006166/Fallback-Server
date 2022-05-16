package me.candiesjar.fallbackserver.commands.base;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.TitleUtil;
import me.candiesjar.fallbackserver.utils.chat.ChatUtil;
import net.kyori.adventure.text.Component;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import static me.candiesjar.fallbackserver.enums.VelocityMessages.*;

public class HubCommand implements SimpleCommand {

    private final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    @Override
    public void execute(Invocation invocation) {

        final CommandSource commandSource = invocation.source();

        if (!(commandSource instanceof Player)) {
            PLAYER_ONLY.send(commandSource,
                    new PlaceHolder("prefix", PREFIX.color()));
            return;
        }

        final Player player = (Player) commandSource;

        if (FallbackServerVelocity.getInstance().isHub(player.getCurrentServer().get().getServerInfo())) {
            ALREADY_IN_LOBBY.send(player,
                    new PlaceHolder("prefix", PREFIX.color()));
            return;
        }

        final LinkedList<FallingServer> lobbies = new LinkedList<>(FallingServer.getServers().values());

        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        if (lobbies.size() == 0) {
            NO_SERVER.send(player,
                    new PlaceHolder("prefix", PREFIX.color()));
            return;
        }

        final RegisteredServer server = lobbies.get(0).getRegisteredServer();
        player.createConnectionRequest(server).fireAndForget();

        if (USE_HUB_TITLE.get(Boolean.class)) {
            instance.getServer().getScheduler().buildTask(instance, () -> TitleUtil.sendTitle(HUB_TITLE_FADE_IN.get(Integer.class),
                            HUB_TITLE_STAY.get(Integer.class),
                            HUB_TITLE_FADE_OUT.get(Integer.class),
                            ChatUtil.color(HUB_TITLE.get(String.class)),
                            ChatUtil.color(HUB_SUB_TITLE.get(String.class)),
                            player))
                    .delay(HUB_TITLE_DELAY.get(Integer.class), TimeUnit.SECONDS)
                    .schedule();
        } else {
            MOVED_TO_HUB.send(player, new PlaceHolder("prefix", PREFIX.color()), new PlaceHolder("server", server.getServerInfo().getName()));
        }

    }
}
