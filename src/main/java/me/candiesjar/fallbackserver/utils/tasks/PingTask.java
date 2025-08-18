package me.candiesjar.fallbackserver.utils.tasks;

import com.google.common.collect.Lists;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.config.VelocityConfig;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handler.ErrorHandler;
import me.candiesjar.fallbackserver.objects.ServerType;
import me.candiesjar.fallbackserver.utils.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class PingTask {

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();
    private final ServerTypeManager serverTypeManager = fallbackServerVelocity.getServerTypeManager();
    private final OnlineLobbiesManager onlineLobbiesManager = fallbackServerVelocity.getOnlineLobbiesManager();
    private final List<RegisteredServer> lobbyServers = Lists.newArrayList();

    @Getter
    private ScheduledTask scheduledTask;

    public void start(String mode) {
        lobbyServers.clear();

        for (ServerType serverType : serverTypeManager.getServerTypeMap().values()) {
            loadServerList(serverType.getLobbies());
        }

        long timeout = VelocityConfig.PING_TIMEOUT.get(Integer.class);
        Duration duration = Duration.ofSeconds(timeout);
        PingOptions pingOptions = PingOptions.builder().timeout(duration).build();
        int delay = VelocityConfig.PING_DELAY.get(Integer.class);

        if (delay < 1) {
            ErrorHandler.add(Severity.WARNING, "[PING] Ping delay must be greater than 0. Defaulting to 8 seconds.");
            delay = 8;
        }

        if (fallbackServerVelocity.isDebug()) {
            Utils.printDebug("§7[PING] Ping task started with mode: " + mode, false);
            Utils.printDebug("§7[PING] Ping task delay: " + delay + " seconds", false);
            Utils.printDebug("§7[PING] Ping task servers: " + lobbyServers.size(), false);
        }

        switch (mode) {
            case "DEFAULT":
                ErrorHandler.add(Severity.INFO, "[PING] Using default ping mode.");
                scheduledTask = fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> pingServers(false, pingOptions)).repeat(delay, TimeUnit.SECONDS).schedule();
                break;
            case "SOCKET":
                ErrorHandler.add(Severity.INFO, "[PING] Using socket ping mode.");
                fallbackServerVelocity.getLogger().info("§7[§b!§7] Using socket ping mode, this mode will not check the player count.");
                scheduledTask = fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> pingServers(true, pingOptions)).repeat(delay, TimeUnit.SECONDS).schedule();
                break;
            default:
                ErrorHandler.add(Severity.WARNING, "[PING] Invalid ping mode: " + mode);
                scheduledTask = fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> pingServers(false, pingOptions)).repeat(delay, TimeUnit.SECONDS).schedule();
                break;
        }
    }

    @SneakyThrows
    private void pingServers(boolean sockets, PingOptions pingOptions) {
        if (!sockets) {
            ping(pingOptions);
            return;
        }

        lobbyServers.forEach(PingTask::socketPing);
    }

    private void ping(PingOptions pingOptions) {
        lobbyServers.forEach(registeredServer -> registeredServer.ping(pingOptions).whenComplete((result, throwable) -> {
            if (result == null || throwable != null) {
                updateFallingServer(registeredServer, true);
                return;
            }

            Optional<ServerPing.Players> playersOptional = result.getPlayers();

            if (playersOptional.isEmpty()) {
                updateFallingServer(registeredServer, true);
                return;
            }

            ServerPing.Players players = playersOptional.get();

            if (players.getOnline() == players.getMax()) {
                ErrorHandler.add(Severity.INFO, "[PING] " + registeredServer.getServerInfo().getName() + " is full.");
                updateFallingServer(registeredServer, true);
                return;
            }

            updateFallingServer(registeredServer, false);
        }));
    }

    @SneakyThrows
    private void socketPing(RegisteredServer registeredServer) {
        InetSocketAddress socketAddress = registeredServer.getServerInfo().getAddress();
        int port = socketAddress.getPort();
        String address = socketAddress.getAddress().getHostAddress();
        InetAddress inetAddress = InetAddress.getByName(address);

        try (Socket socket = new Socket(inetAddress, port)) {
            if (socket.isConnected()) {
                updateFallingServer(registeredServer, false);
            }
        } catch (IOException exception) {
            ErrorHandler.add(Severity.INFO, "[SOCKET PING] " + registeredServer.getServerInfo().getName() + " is either offline or full.");
            updateFallingServer(registeredServer, true);
        }
    }

    private void updateFallingServer(RegisteredServer registeredServer, boolean remove) {
        String name = registeredServer.getServerInfo().getName();
        String group;

        for (ServerType serverType : serverTypeManager.getServerTypeMap().values()) {
            if (!serverType.getLobbies().contains(name)) {
                continue;
            }

            group = serverType.getName();
            boolean containsValue = onlineLobbiesManager.containsValue(group, registeredServer);

            if (remove) {
                if (!containsValue) {
                    continue;
                }

                ErrorHandler.add(Severity.INFO, "[PING] " + registeredServer.getServerInfo().getName() + " is either offline or full.");

                onlineLobbiesManager.remove(group, registeredServer);
                continue;
            }

            if (containsValue) {
                continue;
            }

            onlineLobbiesManager.put(group, registeredServer);
        }
    }

    private void loadServerList(List<String> serverList) {
        if (serverList == null) return;

        for (String serverName : serverList) {
            RegisteredServer server = fallbackServerVelocity.getServer().getServer(serverName).orElse(null);

            if (server == null) {
                ErrorHandler.add(Severity.ERROR, "[PING] Server " + serverName + " not found.");
                continue;
            }

            ServerInfo serverInfo = getServerInfo(serverName);

            if (serverInfo == null) {
                ErrorHandler.add(Severity.ERROR, "[PING] ServerInfo for " + serverName + " not found.");
                continue;
            }

            if (!lobbyServers.contains(server)) {
                lobbyServers.add(server);
            }
        }
    }

    private ServerInfo getServerInfo(String serverName) {
        return fallbackServerVelocity.getServer().getServer(serverName).map(RegisteredServer::getServerInfo).orElse(null);
    }

    public void reload() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
        String mode = VelocityConfig.PING_STRATEGY.get(String.class);
        start(mode);
    }

}
