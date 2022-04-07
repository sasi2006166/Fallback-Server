package me.candiesjar.fallbackserver.velocity.objects;

import com.google.common.collect.Maps;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@Setter
public class VelocityFallingServer implements Comparable<VelocityFallingServer> {

    private static final Map<RegisteredServer, VelocityFallingServer> servers = Maps.newHashMap();
    private final RegisteredServer registeredServer;

    public VelocityFallingServer(RegisteredServer registeredServer) {
        this.registeredServer = registeredServer;
        servers.put(registeredServer, this);
    }

    public static Map<RegisteredServer, VelocityFallingServer> getServers() {
        return servers;
    }

    @Override
    public int compareTo(@NotNull VelocityFallingServer o) {
        return Integer.compare(getRegisteredServer().getPlayersConnected().size(), o.getRegisteredServer().getPlayersConnected().size());
    }
}