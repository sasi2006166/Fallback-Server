package me.candiesjar.fallbackserver.enums;

import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;

import java.util.List;

@Getter
public enum BungeeServers {

    SERVERS("servers")

    ;

    private final String path;
    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();

    BungeeServers(String path) {
        this.path = path;
    }

    public List<String> getStringList() {
        return fallbackServerBungee.getServersTextFile().getConfig().getStringList(getPath());
    }

}
