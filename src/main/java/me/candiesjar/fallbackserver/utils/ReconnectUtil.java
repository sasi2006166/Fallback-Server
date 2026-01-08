package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
import me.candiesjar.fallbackserver.handlers.ReconnectHandler;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

@UtilityClass
public class ReconnectUtil {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final PlayerCacheManager playerCacheManager = fallbackServerBungee.getPlayerCacheManager();

    public ServerInfo checkForPhysicalServer() {
        boolean usePhysicalServer = BungeeConfig.RECONNECT_USE_SERVER.getBoolean();

        if (usePhysicalServer) {
            ServerInfo reconnectServer = fallbackServerBungee.getProxy().getServerInfo(BungeeConfig.RECONNECT_SERVER.getString());

            if (reconnectServer == null) {
                ErrorHandler.add(Severity.ERROR, "[RECONNECT] The server " + BungeeConfig.RECONNECT_SERVER.getString() + " does not exist!");
                Utils.printDebug("Server " + BungeeConfig.RECONNECT_SERVER.getString() + " does not exist!", true);
                Utils.printDebug("Check config.yml for typos, then restart your proxy.", true);
                Utils.printDebug("Using limbo mode.", true);
                return null;
            }

            return reconnectServer;
        }

        return null;
    }

    public void cancelReconnect(UUID uuid) {
        ReconnectHandler task = playerCacheManager.remove(uuid);

        if (task == null) {
            return;
        }

        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("Cancelling reconnect task for UUID " + uuid, true);
        }

        task.getReconnectTask().cancel();

        if (task.getTitleTask() != null) {
            task.getTitleTask().cancel();
        }

        if (task.getConnectTask() != null) {
            task.getConnectTask().cancel();
        }

        ErrorHandler.add(Severity.INFO, "[RECONNECT] Reconnect task for player " + uuid + " has been cancelled.");
    }
}
