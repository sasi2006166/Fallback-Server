package me.candiesjar.fallbackserver.bungee.objects;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;

@Getter
@Setter
public class FallingServer implements Comparable<FallingServer> {

    private static final List<FallingServer> servers = Lists.newArrayList();
    private final ServerInfo serverInfo;

    public FallingServer(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        servers.add(this);
    }

    @Override
    public int compareTo(FallingServer o) {
        return Integer.compare(getServerInfo().getPlayers().size(), o.getServerInfo().getPlayers().size());
    }

    public static List<FallingServer> getServers() {
        return servers;
    }
}