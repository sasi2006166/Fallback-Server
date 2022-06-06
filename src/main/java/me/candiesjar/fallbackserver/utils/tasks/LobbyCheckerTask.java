package me.candiesjar.fallbackserver.utils.tasks;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.objects.FallingServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;

public class LobbyCheckerTask implements Runnable {
    private static final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    @Override
    public void run() {
        FallingServer.getServers().clear();
        for (String serverName : BungeeConfig.LOBBIES_LIST.getStringList()) {
            ServerInfo serverInfo = instance.getProxy().getServerInfo(serverName);

            if (serverInfo == null) {
                continue;
            }

            serverInfo.ping((result, error) -> {

                if (error == null) {
                    if (result.getPlayers().getOnline() == result.getPlayers().getMax()) {
                        instance.getLogger().info("Lobby " + serverInfo.getName() + " is full!");
                        return;
                    }
                    new FallingServer(serverInfo);
                }
            });
        }
    }
}
