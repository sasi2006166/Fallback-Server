package me.candiesjar.fallbackserver.listeners;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerDisconnectListener implements Listener {

    @EventHandler
    public void onServerDisconnect(ServerDisconnectEvent event) {

        if (!event.getTarget().getName().equalsIgnoreCase("testsv")) {
            return;
        }

        System.out.println("Disconnect event");


        UserConnection player = (UserConnection) event.getPlayer();

        player.connectNow(ProxyServer.getInstance().getServerInfo("lobby1"), ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT);


    }

}
