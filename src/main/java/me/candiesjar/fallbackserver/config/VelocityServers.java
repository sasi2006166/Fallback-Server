package me.candiesjar.fallbackserver.config;

import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerVelocity;

import java.util.List;

@Getter
public enum VelocityServers {

    SERVERS("servers")

    ;

    private final String path;
    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();

    VelocityServers(String path) {
        this.path = path;
    }

    public List<String> getStringList() {
        return fallbackServerVelocity.getServersTextFile().getConfig().getStringList(getPath());
    }

}
