package me.candiesjar.fallbackserver.objects;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FallingServer implements Comparable<FallingServer> {
    private final RegisteredServer registeredServer;

    @Override
    public int compareTo(FallingServer o) {
        return Integer.compare(getRegisteredServer().getPlayersConnected().size(), o.getRegisteredServer().getPlayersConnected().size());
    }
}