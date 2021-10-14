package me.candiesjar.fallbackserver.velocity.utils;

import me.candiesjar.fallbackserver.velocity.FallbackServerVelocity;

import java.util.List;

public enum VelocityFields {

    NOT_PLAYER("Messages.not_player");

    private final String path;

    VelocityFields(String path) {
        this.path = path;
    }

    public String getString() {
        return FallbackServerVelocity.getInstance().getConfigFile().getString(path);
    }

    public List<String> getStringList() {
        return FallbackServerVelocity.getInstance().getConfigFile().getStringList(path);
    }
}
