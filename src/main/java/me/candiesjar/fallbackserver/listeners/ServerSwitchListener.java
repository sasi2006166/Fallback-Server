package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
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

        boolean isReconnecting = plugin.getPlayerCacheManager().containsKey(uuid);

        if (isReconnecting) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            FallbackLimboHandler limboHandler = plugin.getPlayerCacheManager().get(uuid);
            LimboPlayer limboPlayer = limboHandler.getLimboPlayer();
            limboPlayer.disconnect(event.getOriginalServer());
            ReconnectUtil.cancelReconnect(uuid);
            return;
        }

        boolean clearChat = VelocityConfig.CLEAR_CHAT_SERVER_SWITCH.get(Boolean.class);

        if (clearChat) {
            ChatUtil.clearChat(player);
        }

    }

}
