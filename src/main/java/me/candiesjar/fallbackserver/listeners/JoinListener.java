package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.objects.FallingServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class JoinListener implements Listener {

    @EventHandler
    public void onServerConnect(final ServerConnectEvent event) {

        if (event.isCancelled()) {
            return;
        }

        if (!event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            return;
        }

        final ProxiedPlayer proxiedPlayer = event.getPlayer();
        final ServerInfo connectTo = event.getTarget();

        if (!proxiedPlayer.isConnected()) {
            return;
        }

        Map<ServerInfo, FallingServer> clonedMap = new HashMap<>(FallingServer.getServers());

        LinkedList<FallingServer> lobbies = new LinkedList<>(clonedMap.values());
        lobbies.sort(FallingServer::compareTo);
        lobbies.sort(Comparator.reverseOrder());

        if (lobbies.size() == 1) {
            return;
        }

        ServerInfo serverInfo = lobbies.get(0).getServerInfo();
        event.setTarget(serverInfo);


    }

}
