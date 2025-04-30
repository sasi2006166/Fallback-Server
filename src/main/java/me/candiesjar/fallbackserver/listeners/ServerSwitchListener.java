package me.candiesjar.fallbackserver.listeners;

import lombok.SneakyThrows;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.connection.FallbackBridge;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.utils.ReconnectUtil;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
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
        if (event.getFrom() == null) {
            return;
        }

        UserConnection user = (UserConnection) event.getPlayer();
        UUID uuid = user.getUniqueId();
        ServerConnection server = user.getServer();
        ChannelWrapper channelWrapper = server.getCh();

        Field handlerField = HandlerBoss.class.getDeclaredField("handler");
        handlerField.setAccessible(true);

        if (playerCacheManager.containsKey(uuid)) {
            ServerInfo reconnectServer = plugin.getReconnectServer();
            ServerInfo actualServer = user.getServer().getInfo();

            if (reconnectServer != actualServer) {
                removeFromReconnect(user);
            }
        }

        boolean clearChat = BungeeConfig.CLEAR_CHAT_SERVER_SWITCH.getBoolean();

        if (clearChat) {
            ChatUtil.clearChat(user);
        }

        if (plugin.isDebug()) {
            Utils.printDebug("[SWITCH] Player " + user.getName() + " switched from " + event.getFrom().getName() + " to " + event.getPlayer().getServer().getInfo().getName(), false);
        }

        FallbackBridge fallbackBridge = new FallbackBridge(proxyServer, user, server);
        channelWrapper.getHandle().pipeline().get(HandlerBoss.class).setHandler(fallbackBridge);
    }

    private void removeFromReconnect(ProxiedPlayer player) {
        BungeeMessages.EXITING_RECONNECT.send(player);
        ReconnectUtil.cancelReconnect(player.getUniqueId());
    }

}
