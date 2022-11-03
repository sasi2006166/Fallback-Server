package me.candiesjar.fallbackserver.listeners;

import com.google.common.collect.Lists;
import me.candiesjar.fallbackserver.objects.FallingServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Comparator;
import java.util.LinkedList;

public class JoinListener implements Listener {

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {

        if (!event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            return;
        }

        ProxiedPlayer player = event.getPlayer();
        ServerInfo connectTo = event.getTarget();

        if (!player.isConnected()) {
            return;
        }

        LinkedList<FallingServer> lobbies = Lists.newLinkedList(FallingServer.getServers().values());

        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        if (lobbies.size() == 1) {
            return;
        }

        ServerInfo serverInfo = lobbies.get(0).getServerInfo();
        event.setTarget(serverInfo);


    }

}
