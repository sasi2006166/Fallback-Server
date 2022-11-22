package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;
import me.candiesjar.fallbackserver.utils.ServerUtils;
import me.candiesjar.fallbackserver.utils.WorldUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Optional;

public class ReconnectListener {

    @Subscribe(order = PostOrder.EARLY)
    public void onPlayerKick(KickedFromServerEvent event, Continuation continuation) {

        RegisteredServer kickedFrom = event.getServer();
        String serverName = kickedFrom.getServerInfo().getName();
        Player player = event.getPlayer();

        if (!player.isActive() || event.kickedDuringServerConnect()) {
            return;
        }

        boolean isMaintenance = ServerUtils.isMaintenance(kickedFrom);

        if (isMaintenance) {
            return;
        }

        Optional<Component> componentOptional = event.getServerKickReason();
        boolean isEmpty = componentOptional.isEmpty();

        String kickReasonString = isEmpty ? "" : ChatUtil.componentToString(componentOptional.get());

        for (String blacklist : VelocityConfig.IGNORED_REASONS.getStringList()) {

            if (isEmpty) {
                break;
            }

            if (PlainTextComponentSerializer.plainText().serialize(componentOptional.get()).contains(blacklist)) {
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(componentOptional.get()));
                return;
            }

        }

        if (shouldUseBlacklistedServer(serverName)) {
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(kickReasonString)));
            return;
        }

        FallbackLimboHandler fallbackLimboHandler = new FallbackLimboHandler(kickedFrom, player.getUniqueId(), player);
        PlayerCacheManager.getInstance().put(player.getUniqueId(), fallbackLimboHandler);
        WorldUtil.getFallbackWorld().spawnPlayer(player, fallbackLimboHandler);
    }

    private boolean shouldUseBlacklistedServer(String serverName) {
        return VelocityConfig.USE_BLACKLISTED_SERVERS.get(Boolean.class) && VelocityConfig.BLACKLISTED_SERVERS_LIST.getStringList().contains(serverName);
    }

}
