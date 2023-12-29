package me.candiesjar.fallbackserver.utils.tasks;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeServers;
import me.candiesjar.fallbackserver.objects.FallingServer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.List;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class PingTask {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = ProxyServer.getInstance();
    private final List<String> lobbyServers = Lists.newArrayList();

    @Getter
    private ScheduledTask task;

    public void start() {
        lobbyServers.clear();
        FallingServer.clear();
        loadServerList(BungeeConfig.FALLBACK_LIST.getStringList());
        loadServerList(BungeeServers.SERVERS.getStringList());
        int delay = BungeeConfig.PING_DELAY.getInt();
        task = proxyServer.getScheduler().schedule(fallbackServerBungee, PingTask::pingServers, 0, delay, TimeUnit.SECONDS);
    }

    private void pingServers() {
        lobbyServers.forEach(server -> {
            ServerInfo serverInfo = proxyServer.getServerInfo(server);

            if (serverInfo == null) {
                return;
            }

            ping(serverInfo);
        });
    }

    private void ping(ServerInfo serverInfo) {
        serverInfo.ping((result, error) -> {
            if (error != null || result == null) {
                FallingServer.removeServer(serverInfo);
                return;
            }

            int players = result.getPlayers().getOnline();
            int max = result.getPlayers().getMax();

            if (players == max) {
                FallingServer.removeServer(serverInfo);
                return;
            }

            createFallingServer(serverInfo);

        });
    }

    private void loadServerList(List<String> serverList) {

        if (serverList.isEmpty()) {
            return;
        }

        lobbyServers.addAll(serverList);

    }

    private void createFallingServer(ServerInfo serverInfo) {
        new FallingServer(serverInfo);
    }

    public void reload() {
        task.cancel();
        start();
    }

}
