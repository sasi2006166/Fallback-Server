package me.candiesjar.fallbackserver.objects;

import lombok.Getter;

import java.util.List;

@Getter
public class ServerType {

    private final String name;
    private final List<String> servers;
    // TODO: Online lobbies here, preserving ram usage.
    private final List<String> lobbies;
    private final boolean reconnect;

    public ServerType(String name, List<String> servers, List<String> lobbies, boolean reconnect) {
        this.name = name;
        this.servers = servers;
        this.lobbies = lobbies;
        this.reconnect = reconnect;
    }
}
