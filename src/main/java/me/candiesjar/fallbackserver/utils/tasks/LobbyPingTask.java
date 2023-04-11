package me.candiesjar.fallbackserver.utils.tasks;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.objects.FallingServer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class LobbyPingTask {

    private final FallbackServerBungee instance = FallbackServerBungee.getInstance();
    private final ProxyServer proxy = ProxyServer.getInstance();

    public void start() {
        proxy.getScheduler().schedule(instance, LobbyPingTask::pingServers, 0, BungeeConfig.PING_DELAY.getInt(), TimeUnit.SECONDS);
    }

    private void pingServers() {

        List<String> servers = BungeeConfig.LOBBIES_LIST.getStringList();

        servers.forEach(server -> {
            ServerInfo serverInfo = proxy.getServerInfo(server);

            if (serverInfo == null) {
                return;
            }

            serverInfo.ping((result, error) -> {

                if (error != null || result == null) {
                    return;
                }

                int players = result.getPlayers().getOnline();
                int max = result.getPlayers().getMax();

                if (players == max) {
                    return;
                }

                new FallingServer(serverInfo);
            });

        });

    }

}

