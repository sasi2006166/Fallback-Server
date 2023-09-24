package me.candiesjar.fallbackserver.enums;

import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerVelocity;

@Getter
public enum VelocityVersion {

    VERSION("version");


    private final String path;
    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();

    VelocityVersion(String path) {
        this.path = path;
    }

    public String getString() {
        return fallbackServerVelocity.getVersionTextFile().getConfig().getString(getPath());
    }


}
