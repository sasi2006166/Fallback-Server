package me.candiesjar.fallbackserver.bungee.utils.tasks;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.bungee.enums.BungeeConfig;
import me.candiesjar.fallbackserver.bungee.objects.FallingServer;
import net.md_5.bungee.api.config.ServerInfo;

public class LobbyCheckerTask implements Runnable {
    private static final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    @Override
    public void run() {
        FallingServer.getServers().clear();
        for (String serverName : BungeeConfig.LOBBIES.getStringList()) {
            ServerInfo serverInfo = instance.getProxy().getServerInfo(serverName);

            if (serverInfo == null) {
                continue;
            }

            serverInfo.ping((result, error) -> {
                if (error == null) {
                    new FallingServer(serverInfo);
                }
            });
        }
    }
}
