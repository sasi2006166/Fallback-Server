package me.candiesjar.fallbackserver.utils.tasks;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
import me.candiesjar.fallbackserver.objects.ServerType;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@UtilityClass
public class PingTask {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = fallbackServerBungee.getProxy();
    private final ServerTypeManager serverTypeManager = fallbackServerBungee.getServerTypeManager();
    private final OnlineLobbiesManager onlineLobbiesManager = fallbackServerBungee.getOnlineLobbiesManager();
    private final List<ServerInfo> lobbyServers = Lists.newArrayList();

    @Getter
    private ScheduledTask task;

    public void start(String mode) {
        lobbyServers.clear();

        for (ServerType serverType : serverTypeManager.getServerTypeMap().values()) {
            loadServerList(serverType.getLobbies());
        }

        int delay = BungeeConfig.PING_DELAY.getInt();

        if (delay < 1) {
            ErrorHandler.add(Severity.WARNING, "[PING] Ping delay must be greater than 0. Defaulting to 5 seconds.");
            delay = 5;
        }

        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("§7[PING] Ping task started with mode: " + mode, false);
            Utils.printDebug("§7[PING] Ping task delay: " + delay + " seconds", false);
            Utils.printDebug("§7[PING] Ping task servers: " + lobbyServers.size(), false);
        }

        switch (mode) {
            case "DEFAULT":
                task = proxyServer.getScheduler().schedule(fallbackServerBungee, () -> pingServers(false), 2, delay, TimeUnit.SECONDS);
                break;
            case "SOCKET":
                fallbackServerBungee.getLogger().info("§7[§b!§7] Using socket ping mode.");
                task = proxyServer.getScheduler().schedule(fallbackServerBungee, () -> pingServers(true), 2, delay, TimeUnit.SECONDS);
                break;
            default:
                ErrorHandler.add(Severity.WARNING, "[PING] Invalid ping mode: " + mode);
                task = proxyServer.getScheduler().schedule(fallbackServerBungee, () -> pingServers(false), 2, delay, TimeUnit.SECONDS);
                break;
        }
    }

    private void pingServers(boolean sockets) {
        Consumer<ServerInfo> pingMethod = sockets ? PingTask::socketPing : PingTask::ping;
        lobbyServers.forEach(pingMethod);
    }

    private void ping(ServerInfo serverInfo) {
        serverInfo.ping((result, error) -> {
            if (error != null || result == null) {
                updateFallingServer(serverInfo, true);
                return;
            }

            int players = result.getPlayers().getOnline();
            int max = result.getPlayers().getMax();

            if (players == max) {
                updateFallingServer(serverInfo, true);
                return;
            }

            updateFallingServer(serverInfo, false);
        });
    }

    @SneakyThrows
    private void socketPing(ServerInfo serverInfo) {
        SocketAddress socketAddress = serverInfo.getSocketAddress();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        int port = inetSocketAddress.getPort();
        String address = inetSocketAddress.getAddress().getHostAddress();
        InetAddress inetAddress = InetAddress.getByName(address);

        try (Socket socket = new Socket(inetAddress, port)) {
            if (socket.isConnected()) {
                updateFallingServer(serverInfo, false);
            }
        } catch (IOException e) {
            updateFallingServer(serverInfo, true);
        }

    }

    private void updateFallingServer(ServerInfo serverInfo, boolean remove) {
        String name = serverInfo.getName();
        String group;

        for (ServerType serverType : serverTypeManager.getServerTypeMap().values()) {
            if (!serverType.getLobbies().contains(name)) {
                continue;
            }

            group = serverType.getGroupName();
            boolean containsValue = onlineLobbiesManager.containsValue(group, serverInfo);

            if (remove) {
                if (!containsValue) {
                    continue;
                }

                ErrorHandler.add(Severity.INFO, "[PING] " + serverInfo.getName() + " is either offline or full.");

                onlineLobbiesManager.remove(group, serverInfo);
                continue;
            }

            if (containsValue) {
                continue;
            }

            onlineLobbiesManager.put(group, serverInfo);
        }
    }

    private void loadServerList(List<String> serverList) {
        if (serverList.isEmpty()) return;

        for (String serverName : serverList) {
            ServerInfo serverInfo = proxyServer.getServerInfo(serverName);

            if (serverInfo == null) {
                ErrorHandler.add(Severity.WARNING, "[PING] Server " + serverName + " not found.");
                continue;
            }

            if (!lobbyServers.contains(serverInfo)) {
                lobbyServers.add(serverInfo);
            }
        }
    }

    public void reload() {
        task.cancel();
        String mode = BungeeConfig.PING_STRATEGY.getString();
        start(mode);
    }
}
