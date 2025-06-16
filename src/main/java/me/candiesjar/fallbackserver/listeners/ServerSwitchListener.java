package me.candiesjar.fallbackserver.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;
import me.candiesjar.fallbackserver.utils.ReconnectUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.elytrium.limboapi.api.player.LimboPlayer;

import java.util.UUID;

@RequiredArgsConstructor
public class ServerSwitchListener {

    private final FallbackServerVelocity plugin;

    @Subscribe
    public void onServerSwitch(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (event.getOriginalServer() == null) {
            return;
        }

        RegisteredServer originalServer = event.getOriginalServer();
        boolean isReconnecting = plugin.getPlayerCacheManager().containsKey(uuid);
        if (!isReconnecting) {
            if (originalServer.getServerInfo().getName().equals("FallbackLimbo")) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
            }
            return;
        }

        FallbackLimboHandler limboHandler = plugin.getPlayerCacheManager().get(uuid);
        RegisteredServer reconnectServer = limboHandler.getTarget();

        if (originalServer.getServerInfo().equals(reconnectServer.getServerInfo())) {
            return;
        }

        boolean physical = VelocityConfig.RECONNECT_USE_PHYSICAL.get(Boolean.class);
        if (physical) {
            String physicalServerName = VelocityConfig.RECONNECT_PHYSICAL_SERVER.get(String.class);
            RegisteredServer physicalServer = plugin.getServer().getServer(physicalServerName).orElse(null);
            if (physicalServer != null && originalServer.getServerInfo().equals(physicalServer.getServerInfo())) {
                return;
            }
        }

        if (!originalServer.getServerInfo().getName().equals("FallbackLimbo")) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }

        ReconnectUtil.cancelReconnect(uuid);
        if (!physical) {
            LimboPlayer limboPlayer = limboHandler.getLimboPlayer();
            limboPlayer.disconnect(originalServer);
            return;
        }

        if (!originalServer.getServerInfo().getName().equals("FallbackLimbo")) {
            player.createConnectionRequest(originalServer).fireAndForget();
            return;
        }

        requestKick(player);
    }

    @Subscribe
    public void onServerSwitched(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        boolean clearChat = VelocityConfig.CLEAR_CHAT_SERVER_SWITCH.get(Boolean.class);
        if (clearChat) {
            ChatUtil.clearChat(player);
        }
    }

    private void requestKick(Player player) {
        ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF(player.getUniqueId().toString());
        player.getCurrentServer().ifPresent(sv ->
                sv.sendPluginMessage(plugin.getReconnectIdentifier(), buf.toByteArray()));
    }
}
