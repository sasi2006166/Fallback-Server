package me.candiesjar.fallbackserver.objects;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Map;

@Getter
public class FallingServer implements Comparable<FallingServer> {

    @Getter
    private static final Map<ServerInfo, FallingServer> servers = Maps.newHashMap();

    private final ServerInfo serverInfo;

    public FallingServer(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        servers.put(serverInfo, this);
    }

    @Override
    public int compareTo(FallingServer o) {
        return Integer.compare(getServerInfo().getPlayers().size(), o.getServerInfo().getPlayers().size());
    }

    public static void removeServer(ServerInfo serverInfo) {
        servers.remove(serverInfo);
    }
}
