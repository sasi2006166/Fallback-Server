package me.candiesjar.fallbackserver.enums;

import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;

@Getter
public enum BungeeVersion {

    VERSION("version");

    private final String path;
    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();

    BungeeVersion(String path) {
        this.path = path;
    }

    public String getString() {
        return fallbackServerBungee.getVersionConfig().getString(getPath());
    }

}
