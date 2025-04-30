package me.candiesjar.fallbackserver.config;

import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;

import java.util.List;

@Getter
public enum BungeeServers {

    SERVERS("servers");

    private final String path;
    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();

    BungeeServers(String path) {
        this.path = path;
    }

    public List<String> getStringList() {
        return fallbackServerBungee.getServersConfig().getStringList(getPath());
    }

}
