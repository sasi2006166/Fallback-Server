package me.candiesjar.fallbackserver.utils.tasks;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.ServerCacheManager;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import me.candiesjar.fallbackserver.enums.BungeeServers;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.List;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class PingTask {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = ProxyServer.getInstance();
    private final ServerCacheManager serverCacheManager = fallbackServerBungee.getServerCacheManager();
    private final List<String> lobbyServers = Lists.newArrayList();

    @Getter
    private ScheduledTask task;

    public void start() {
        lobbyServers.clear();
        serverCacheManager.clear();
        loadServerList(BungeeServers.SERVERS.getStringList());
        loadServerList(BungeeConfig.FALLBACK_LIST.getStringList());
        int delay = BungeeConfig.PING_DELAY.getInt();
        task = proxyServer.getScheduler().schedule(fallbackServerBungee, PingTask::pingServers, 0, delay, TimeUnit.SECONDS);
    }

    private void pingServers() {
        lobbyServers.forEach(server -> {
            ServerInfo serverInfo = proxyServer.getServerInfo(server);
            ping(serverInfo);
        });
    }

    private void ping(ServerInfo serverInfo) {
        serverInfo.ping((result, error) -> {
            if (error != null || result == null) {
                serverCacheManager.remove(serverInfo);
                return;
            }

            int players = result.getPlayers().getOnline();
            int max = result.getPlayers().getMax();

            if (players == max) {
                serverCacheManager.remove(serverInfo);
                return;
            }

            if (serverCacheManager.containsKey(serverInfo)) {
                return;
            }

            Utils.printDebug("Serverinfo " + serverInfo + " passed all errors.", true);
            Utils.printDebug("Adding server " + serverInfo.getName() + " to cache", false);

            serverCacheManager.put(serverInfo, true);

        });
    }

    private void loadServerList(List<String> serverList) {
        for (String serverName : serverList) {
            ServerInfo serverInfo = getServerInfo(serverName);

            if (serverInfo == null) {
                continue;
            }

            lobbyServers.add(serverName);
        }
    }

    private ServerInfo getServerInfo(String serverName) {
        return proxyServer.getServerInfo(serverName);
    }

    public void reload() {
        task.cancel();
        start();
    }

}
