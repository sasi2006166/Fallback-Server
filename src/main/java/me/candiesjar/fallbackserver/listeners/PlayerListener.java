package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.VelocityUtils;
import net.kyori.adventure.text.Component;

import java.util.UUID;


@RequiredArgsConstructor
public class PlayerListener {

    private final FallbackServerVelocity fallbackServerVelocity;

    @Subscribe
    public void onPlayerJoin(ServerConnectedEvent event) {

        Player player = event.getPlayer();
        String adminPermission = VelocityConfig.ADMIN_PERMISSION.get(String.class);

        if (!player.hasPermission(adminPermission)) {
            return;
        }

        if (fallbackServerVelocity.isAlpha()) {
            return;
        }

        VelocityUtils.getUpdates().whenComplete((newUpdate, throwable) -> {
            if (throwable != null) {
                return;
            }

            if (newUpdate != null && newUpdate) {
                VelocityMessages.NEW_UPDATE.sendList(player,
                        new Placeholder("old_version", FallbackServerVelocity.VERSION),
                        new Placeholder("new_version", VelocityUtils.getRemoteVersion()));
            }
        });
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        FallbackLimboHandler limboHandler = PlayerCacheManager.getInstance().get(uuid);

        if (limboHandler != null) {
            fallbackServerVelocity.cancelReconnect(uuid);
        }

    }

}
