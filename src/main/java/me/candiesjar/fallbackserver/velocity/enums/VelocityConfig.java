package me.candiesjar.fallbackserver.velocity.enums;

import me.candiesjar.fallbackserver.velocity.utils.ConfigurationUtil;

public enum VelocityConfig {
    ;


    private final String path;

    VelocityConfig(String path) {
        this.path = path;
    }

    public String getString() {
        return ConfigurationUtil.getConfig().getString(path);
    }

    public boolean getBoolean() {
        return ConfigurationUtil.getConfig().getBoolean(path);
    }

}
