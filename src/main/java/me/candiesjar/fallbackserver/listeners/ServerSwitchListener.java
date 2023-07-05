package me.candiesjar.fallbackserver.listeners;

import lombok.SneakyThrows;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.connection.FallbackBridge;
import me.candiesjar.fallbackserver.connection.ReconnectBridge;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;

import java.lang.reflect.Field;
import java.util.UUID;

public class ServerSwitchListener implements Listener {

    private final FallbackServerBungee plugin;
    private final PlayerCacheManager playerCacheManager;
    private final ProxyServer proxyServer;

    public ServerSwitchListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
        this.playerCacheManager = plugin.getPlayerCacheManager();
        this.proxyServer = ProxyServer.getInstance();
    }

    @SneakyThrows
    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {

        UserConnection user = (UserConnection) event.getPlayer();
        ProxiedPlayer player = event.getPlayer();

        if (event.getFrom() == null) {
            return;
        }

        UUID uuid = user.getUniqueId();
        ServerConnection server = user.getServer();
        ChannelWrapper channelWrapper = server.getCh();

        Field handlerField = HandlerBoss.class.getDeclaredField("handler");
        handlerField.setAccessible(true);
        DownstreamBridge previousBridge = (DownstreamBridge) handlerField.get(channelWrapper.getHandle().pipeline().get(HandlerBoss.class));

        if (playerCacheManager.containsKey(uuid)) {
            BungeeMessages.EXITING_RECONNECT.send(player);
            plugin.cancelReconnect(uuid);
        }

        if (plugin.isReconnect()) {
            ReconnectBridge reconnectBridge = new ReconnectBridge(proxyServer, user, server, previousBridge);
            channelWrapper.getHandle().pipeline().get(HandlerBoss.class).setHandler(reconnectBridge);
            return;
        }

        FallbackBridge fallbackBridge = new FallbackBridge(proxyServer, user, server, previousBridge);
        channelWrapper.getHandle().pipeline().get(HandlerBoss.class).setHandler(fallbackBridge);

    }

}
