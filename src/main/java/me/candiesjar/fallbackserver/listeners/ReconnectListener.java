package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;
import me.candiesjar.fallbackserver.utils.ServerUtils;
import me.candiesjar.fallbackserver.utils.WorldUtil;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Optional;

public class ReconnectListener {

    @Subscribe(order = PostOrder.EARLY)
    public void onPlayerKick(LoginLimboRegisterEvent event) {

        event.setOnKickCallback(kickEvent -> {

            RegisteredServer kickedFrom = kickEvent.getServer();
            String serverName = kickedFrom.getServerInfo().getName();
            Player player = event.getPlayer();
            ConnectedPlayer connectedPlayer = (ConnectedPlayer) player;

            if (!player.isActive() || kickEvent.kickedDuringServerConnect()) {
                return false;
            }

            boolean isMaintenance = ServerUtils.isMaintenance(kickedFrom);

            if (isMaintenance) {
                return false;
            }

            Optional<Component> componentOptional = kickEvent.getServerKickReason();
            boolean isEmpty = componentOptional.isEmpty();

            String kickReasonString = isEmpty ? "" : ChatUtil.componentToString(componentOptional.get());

            for (String blacklist : VelocityConfig.IGNORED_REASONS.getStringList()) {

                if (isEmpty) {
                    break;
                }

                if (PlainTextComponentSerializer.plainText().serialize(componentOptional.get()).contains(blacklist)) {
                    player.disconnect(Component.text(kickReasonString));
                    return false;
                }

            }

            if (shouldUseBlacklistedServer(serverName)) {
                player.disconnect(Component.text(kickReasonString));
                return false;
            }

            connectedPlayer.getTabList().clearAll();
            connectedPlayer.clearTitle();
            FallbackLimboHandler fallbackLimboHandler = new FallbackLimboHandler(kickedFrom, player.getUniqueId(), player);
            PlayerCacheManager.getInstance().put(player.getUniqueId(), fallbackLimboHandler);
            WorldUtil.getFallbackWorld().spawnPlayer(player, fallbackLimboHandler);

            return true;
        });

    }

    private boolean shouldUseBlacklistedServer(String serverName) {
        return VelocityConfig.USE_BLACKLISTED_SERVERS.get(Boolean.class) && VelocityConfig.BLACKLISTED_SERVERS_LIST.getStringList().contains(serverName);
    }

}
