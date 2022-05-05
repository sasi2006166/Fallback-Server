package me.candiesjar.fallbackserver.commands.base;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.FallingServer;
import me.candiesjar.fallbackserver.utils.TitleUtil;

import java.util.Comparator;
import java.util.LinkedList;

public class HubCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {

        CommandSource commandSource = invocation.source();

        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(VelocityMessages.colorize(VelocityMessages.ONLY_PLAYER.get(String.class)
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        Player player = (Player) commandSource;

        if (FallbackServerVelocity.getInstance().isHub(player.getCurrentServer().get().getServerInfo())) {
            player.sendMessage(VelocityMessages.colorize(VelocityMessages.ALREADY_IN_HUB.get(String.class)
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        LinkedList<FallingServer> lobbies = new LinkedList<>(FallingServer.getServers().values());

        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        if (lobbies.size() == 0) {
            player.sendMessage(VelocityMessages.colorize(VelocityMessages.NO_SERVER.get(String.class)
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
            return;
        }

        RegisteredServer server = lobbies.get(0).getRegisteredServer();
        player.createConnectionRequest(server).fireAndForget();

        if (VelocityMessages.USE_HUB_TITLE.get(Boolean.class)) {
            TitleUtil.sendHubTitle(player);
        } else {
            player.sendMessage(VelocityMessages.colorize(VelocityMessages.CONNECT_TO_HUB.get(String.class)
                    .replace("%prefix%", VelocityMessages.PREFIX.color())));
        }

    }
}
