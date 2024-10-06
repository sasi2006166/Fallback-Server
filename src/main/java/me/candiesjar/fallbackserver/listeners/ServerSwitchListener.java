package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;
import me.candiesjar.fallbackserver.utils.ReconnectUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@RequiredArgsConstructor
public class ServerSwitchListener {

    private final FallbackServerVelocity plugin;

    @Subscribe
    public void onServerSwitch(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        boolean isReconnecting = plugin.getPlayerCacheManager().containsKey(uuid);
        if (!isReconnecting) {
            return;
        }

        if (event.getOriginalServer() == null) {
            return;
        }

        RegisteredServer originalServer = event.getOriginalServer();
        FallbackLimboHandler limboHandler = plugin.getPlayerCacheManager().get(uuid);
        RegisteredServer reconnectServer = limboHandler.getTarget();

        if (originalServer.getServerInfo().equals(reconnectServer.getServerInfo())) {
            return;
        }

        if (!originalServer.getServerInfo().getName().equals("FallbackLimbo")) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }

        LimboPlayer limboPlayer = limboHandler.getLimboPlayer();
        ReconnectUtil.cancelReconnect(uuid);
        limboPlayer.disconnect(originalServer);
    }

    @Subscribe
    public void onServerSwitched(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        boolean clearChat = VelocityConfig.CLEAR_CHAT_SERVER_SWITCH.get(Boolean.class);

        if (clearChat) {
            ChatUtil.clearChat(player);
        }
    }
}
