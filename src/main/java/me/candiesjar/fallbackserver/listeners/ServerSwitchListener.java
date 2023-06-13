package me.candiesjar.fallbackserver.listeners;

import lombok.SneakyThrows;
import me.candiesjar.fallbackserver.connection.FallbackBridge;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;

import java.lang.reflect.Field;

public class ServerSwitchListener implements Listener {

    private final ProxyServer proxyServer = ProxyServer.getInstance();

    @SneakyThrows
    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {

        UserConnection user = (UserConnection) event.getPlayer();
        ServerConnection server = user.getServer();
        ChannelWrapper channelWrapper = server.getCh();

        Field handlerField = HandlerBoss.class.getDeclaredField("handler");
        handlerField.setAccessible(true);
        DownstreamBridge previousBridge = (DownstreamBridge) handlerField.get(channelWrapper.getHandle().pipeline().get(HandlerBoss.class));

        FallbackBridge fallbackBridge = new FallbackBridge(proxyServer, user, server, previousBridge);
        channelWrapper.getHandle().pipeline().get(HandlerBoss.class).setHandler(fallbackBridge);

    }

}
