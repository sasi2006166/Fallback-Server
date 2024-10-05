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
import me.candiesjar.fallbackserver.enums.VelocityConfig;
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
        int taskDelay = VelocityConfig.PING_DELAY.get(Integer.class);

        switch (mode) {
            case "DEFAULT":
                scheduledTask = fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> pingServers(false, pingOptions)).repeat(taskDelay, TimeUnit.SECONDS).schedule();
                break;
            case "SOCKET":
                scheduledTask = fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> pingServers(true, pingOptions)).repeat(taskDelay, TimeUnit.SECONDS).schedule();
                break;
            default:
                fallbackServerVelocity.getLogger().error("[!] Configuration error, using default ping mode.");
                scheduledTask = fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> pingServers(false, pingOptions)).repeat(taskDelay, TimeUnit.SECONDS).schedule();
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
                updateFallingServer(registeredServer, true);
                return;
            }

            updateFallingServer(registeredServer, false);
        }));
    }

    private void socketPing(RegisteredServer registeredServer) {
        InetSocketAddress socketAddress = registeredServer.getServerInfo().getAddress();
        int port = socketAddress.getPort();
        String address = socketAddress.getAddress().getHostAddress();

        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            Socket socket = new Socket(inetAddress, port);

            if (socket.isConnected()) {
                updateFallingServer(registeredServer, false);
            }

            socket.close();
        } catch (IOException exception) {
            updateFallingServer(registeredServer, true);
            if (fallbackServerVelocity.isDebug()) {
                Utils.printDebug("§7[§c!§7] Error while pinging server: " + registeredServer.getServerInfo().getName(), true);
            }
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
        if (serverList == null) {
            return;
        }

        for (String serverName : serverList) {
            RegisteredServer server = fallbackServerVelocity.getServer().getServer(serverName).orElse(null);

            if (server == null) {
                continue;
            }

            ServerInfo serverInfo = getServerInfo(serverName);

            if (serverInfo == null) {
                continue;
            }

            if (lobbyServers.contains(server)) {
                continue;
            }

            lobbyServers.add(server);
        }
    }

    private ServerInfo getServerInfo(String serverName) {
        return fallbackServerVelocity.getServer().getServer(serverName).map(RegisteredServer::getServerInfo).orElse(null);
    }

    public void reload() {
        String mode = VelocityConfig.PING_MODE.get(String.class);
        start(mode);
    }

}
