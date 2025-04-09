package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.PlayerCacheManager;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.handlers.FallbackReconnectHandler;
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
                Utils.printDebug("The server " + BungeeConfig.RECONNECT_SERVER.getString() + " does not exist!", true);
                Utils.printDebug("Check your config.yml file for more infos.", true);
                Utils.printDebug("Please add it and RESTART your proxy.", true);
                Utils.printDebug("Moving to limbo mode instead.", true);
                return null;
            }

            return reconnectServer;
        }

        return null;
    }

    public void cancelReconnect(UUID uuid) {
        FallbackReconnectHandler task = playerCacheManager.remove(uuid);

        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("Cancelling reconnect task for player " + uuid, true);
        }

        if (task == null) {
            return;
        }

        task.getReconnectTask().cancel();

        if (task.getTitleTask() != null) {
            task.getTitleTask().cancel();
        }

        if (task.getConnectTask() != null) {
            task.getConnectTask().cancel();
        }
    }
}
