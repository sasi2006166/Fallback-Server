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
public class PingTask {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = ProxyServer.getInstance();

    public void start() {
        proxyServer.getScheduler().schedule(fallbackServerBungee, PingTask::pingServers, 0, BungeeConfig.PING_DELAY.getInt(), TimeUnit.SECONDS);
    }

    private void pingServers() {
        FallingServer.getServers().clear();
        List<String> lobbyServers = BungeeConfig.LOBBIES_LIST.getStringList();

        lobbyServers.forEach(server -> {
            ServerInfo serverInfo = proxyServer.getServerInfo(server);

            if (serverInfo == null) {
                return;
            }

            pingServer(serverInfo);
        });
    }

    private void pingServer(ServerInfo serverInfo) {
        serverInfo.ping((result, error) -> {
            if (error != null || result == null) {
                return;
            }

            int players = result.getPlayers().getOnline();
            int max = result.getPlayers().getMax();

            if (players == max) {
                return;
            }

            createFallingServer(serverInfo);
        });
    }

    private void createFallingServer(ServerInfo serverInfo) {
        new FallingServer(serverInfo);
    }
}
