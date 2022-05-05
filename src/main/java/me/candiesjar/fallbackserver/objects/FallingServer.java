package me.candiesjar.fallbackserver.objects;

import com.google.common.collect.Maps;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@Setter
public class FallingServer implements Comparable<FallingServer> {
    private static final Map<String, FallingServer> servers = Maps.newHashMap();
    private final RegisteredServer registeredServer;

    public FallingServer(RegisteredServer registeredServer) {
        this.registeredServer = registeredServer;
        servers.put(registeredServer.getServerInfo().getName().toLowerCase(), this);
    }

    public static Map<String, FallingServer> getServers() {
        return servers;
    }

    @Override
    public int compareTo(@NotNull FallingServer o) {
        return Integer.compare(getRegisteredServer().getPlayersConnected().size(), o.getRegisteredServer().getPlayersConnected().size());
    }
}