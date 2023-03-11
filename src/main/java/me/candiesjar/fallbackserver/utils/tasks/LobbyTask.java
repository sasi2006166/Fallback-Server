package me.candiesjar.fallbackserver.utils.tasks;

import com.google.common.collect.Lists;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import lombok.RequiredArgsConstructor;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.objects.server.impl.FallingServerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class LobbyTask implements Runnable {

    private final FallingServerManager fallingServerManager;

    public List<String> getAllowedServers() {
        List<String> allowedServers = new ArrayList<>();

        for (String serverName : VelocityConfig.LOBBIES_LIST.getStringList()) {
            String toLowerCase = serverName.toLowerCase();
            allowedServers.add(toLowerCase);
        }

        return allowedServers;
    }

    @Override
    public void run() {
        fallingServerManager.clearCache();

        List<RegisteredServer> servers = Lists.newArrayList();
        List<String> allowedServers = getAllowedServers();

        for (RegisteredServer server : FallbackServerVelocity.getInstance().getServer().getAllServers()) {
            ServerInfo serverInfo = server.getServerInfo();
            String serverName = serverInfo.getName().toLowerCase();

            if (!allowedServers.contains(serverName)) {
                continue;
            }

            servers.add(server);
        }

        pingServers(servers);
    }

    private void pingServers(List<RegisteredServer> serverList) {
        serverList.forEach(server -> server.ping().whenComplete((result, throwable) -> {
            if (throwable != null) {
                return;
            }

            if (result == null) {
                return;
            }

            Optional<ServerPing.Players> playersOptional = result.getPlayers();

            if (playersOptional.isEmpty()) {
                return;
            }

            ServerPing.Players players = playersOptional.get();

            if (players.getOnline() == players.getMax()) {
                return;
            }

            fallingServerManager.add(server.getServerInfo().getName(), server);
        }));
    }
}
