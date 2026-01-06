package me.candiesjar.fallbackserver.tasks;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
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
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PingTask {

    private final FallbackServerBungee fallbackServerBungee;
    private final ProxyServer proxyServer;
    private final TaskScheduler taskScheduler;
    private final ServerTypeManager serverTypeManager;
    private final OnlineLobbiesManager onlineLobbiesManager;
    private final List<ServerInfo> lobbyServers;

    public PingTask(FallbackServerBungee plugin) {
        this.fallbackServerBungee = plugin;
        this.proxyServer = plugin.getProxy();
        this.taskScheduler = proxyServer.getScheduler();
        this.serverTypeManager = plugin.getServerTypeManager();
        this.onlineLobbiesManager = plugin.getOnlineLobbiesManager();
        this.lobbyServers = Lists.newArrayList();
    }

    @Getter
    private ScheduledTask task;

    public void start(String mode) {
        lobbyServers.clear();

        for (ServerType serverType : serverTypeManager.getServerTypeMap().values()) {
            loadServerList(serverType.getLobbies());
        }

        int delay = BungeeConfig.PING_DELAY.getInt();

        if (delay < 1) {
            ErrorHandler.add(Severity.WARNING, "[PING] Ping delay must be greater than 0. Defaulting to 8 seconds.");
            delay = 8;
        }

        if (fallbackServerBungee.isDebug()) {
            Utils.printDebug("§7[PING] Ping task started with mode: " + mode, false);
            Utils.printDebug("§7[PING] Ping task delay: " + delay + " seconds", false);
            Utils.printDebug("§7[PING] Ping task servers: " + lobbyServers.size(), false);
        }

        switch (mode) {
            case "DEFAULT":
                ErrorHandler.add(Severity.INFO, "[PING] Using default ping mode.");
                task = taskScheduler.schedule(fallbackServerBungee, () -> pingServers(false), 2, delay, TimeUnit.SECONDS);
                break;
            case "SOCKET":
                ErrorHandler.add(Severity.INFO, "[PING] Using socket ping mode.");
                fallbackServerBungee.getLogger().info("§7[§b!§7] Using socket ping mode, this mode will not check the player count.");
                task = taskScheduler.schedule(fallbackServerBungee, () -> pingServers(true), 2, delay, TimeUnit.SECONDS);
                break;
            default:
                ErrorHandler.add(Severity.ERROR, "[PING] Invalid ping mode: " + mode);
                task = taskScheduler.schedule(fallbackServerBungee, () -> pingServers(false), 2, delay, TimeUnit.SECONDS);
                break;
        }
    }

    private void pingServers(boolean sockets) {
        Consumer<ServerInfo> pingMethod = sockets ? this::socketPing : this::ping;
        lobbyServers.forEach(pingMethod);
    }

    private void ping(ServerInfo serverInfo) {
        serverInfo.ping((result, error) -> {
            if (error != null || result == null) {
                updateServerStatus(serverInfo, true);
                return;
            }

            int players = result.getPlayers().getOnline();
            int max = result.getPlayers().getMax();

            boolean fullOrOffline = players >= max;

            updateServerStatus(serverInfo, fullOrOffline);
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
                updateServerStatus(serverInfo, false);
            }
        } catch (IOException e) {
            updateServerStatus(serverInfo, true);
        }

    }

    private void updateServerStatus(ServerInfo serverInfo, boolean remove) {
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

                ErrorHandler.add(Severity.WARNING, "[PING] " + serverInfo.getName() + " is either offline or full.");

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
                ErrorHandler.add(Severity.ERROR, "[PING] Server " + serverName + " not found.");
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
