package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handler.ErrorHandler;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;

import java.util.UUID;

@UtilityClass
public class ReconnectUtil {

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();
    private final PlayerCacheManager playerCacheManager = fallbackServerVelocity.getPlayerCacheManager();

    public void cancelReconnect(UUID uuid) {
        FallbackLimboHandler limboHandler = playerCacheManager.remove(uuid);

        if (limboHandler == null) {
            return;
        }

        if (limboHandler.getReconnectTask() != null) {
            limboHandler.getReconnectTask().cancel();
        }

        if (limboHandler.getTitleTask() != null) {
            limboHandler.getTitleTask().cancel();
        }

        if (limboHandler.getConnectTask() != null) {
            limboHandler.getConnectTask().cancel();
        }

        ErrorHandler.add(Severity.INFO, "[RECONNECT] Reconnect task for player " + uuid + " has been cancelled.");

        limboHandler.clearPlayerTitle();
    }
}
