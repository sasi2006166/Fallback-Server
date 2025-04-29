package me.candiesjar.fallbackserver.objects;

import lombok.Getter;

import java.util.List;

@Getter
public class ServerType {

    private final String groupName;
    private final List<String> servers;
    // TODO: Online lobbies here, preserving ram usage.
    private final List<String> lobbies;
    private final boolean reconnect;

    public ServerType(String name, List<String> servers, List<String> lobbies, boolean reconnect) {
        this.groupName = name;
        this.servers = servers;
        this.lobbies = lobbies;
        this.reconnect = reconnect;
    }

    @Override
    public String toString() {
        return "ServerType{" +
                "name='" + groupName + '\'' +
                ", servers=" + servers +
                ", lobbies=" + lobbies +
                ", reconnect=" + reconnect +
                '}';
    }
}
