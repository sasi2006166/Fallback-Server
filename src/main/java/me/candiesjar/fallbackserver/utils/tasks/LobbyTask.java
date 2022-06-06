package me.candiesjar.fallbackserver.utils.tasks;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;
import me.candiesjar.fallbackserver.objects.FallingServer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LobbyTask implements Runnable {

    private static final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    @Override
    public void run() {
        FallingServer.getServers().clear();
        List<String> allowedServers = VelocityConfig.LOBBIES.getStringList().parallelStream().map(String::toLowerCase).collect(Collectors.toList());

        List<RegisteredServer> servers = new ArrayList<>();

        for (RegisteredServer server : instance.getServer().getAllServers()) {
            if (!allowedServers.contains(server.getServerInfo().getName().toLowerCase())) continue;
            servers.add(server);
        }

        servers.forEach(server -> server.ping().whenComplete((result, throwable) -> {
            if (throwable != null) {
                return;
            }

            if (result != null) {
                if (result.getPlayers().get().getOnline() == result.getPlayers().get().getMax()) {
                    return;
                }

                new FallingServer(server);
            }
        }));
    }
}
