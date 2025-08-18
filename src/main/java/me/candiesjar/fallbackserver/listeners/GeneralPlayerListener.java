package me.candiesjar.fallbackserver.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.config.VelocityMessages;
import me.candiesjar.fallbackserver.handler.ErrorHandler;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.ReconnectUtil;
import me.candiesjar.fallbackserver.utils.Utils;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;

import java.util.UUID;

@RequiredArgsConstructor
public class GeneralPlayerListener {

    private final FallbackServerVelocity plugin;

    @Subscribe
    public void onPlayerJoin(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String adminPermission = VelocityConfig.ADMIN_PERMISSION.get(String.class);

        if (!player.hasPermission(adminPermission)) {
            return;
        }

        if (plugin.isOutdated()) {
            VelocityMessages.OUTDATED.send(player, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
        }

        if (!ErrorHandler.getDiagnostics().isEmpty()) {
            VelocityMessages.ERRORS_FOUND.send(player, new Placeholder("prefix", ChatUtil.getFormattedString(VelocityMessages.PREFIX)));
            ErrorHandler.handle();
        }

        Utils.getUpdates().whenComplete((newUpdate, throwable) -> {
            if (throwable != null) {
                return;
            }

            if (newUpdate != null && newUpdate) {
                VelocityMessages.NEW_UPDATE.sendList(player,
                        new Placeholder("old_version", plugin.getVersion()),
                        new Placeholder("new_version", Utils.getRemoteVersion()));
            }
        });
    }

    @Subscribe(priority = Short.MAX_VALUE)
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        FallbackLimboHandler limboHandler = plugin.getPlayerCacheManager().get(uuid);

        if (limboHandler != null) {
            ReconnectUtil.cancelReconnect(uuid);
        }
    }

}
