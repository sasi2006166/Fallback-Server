package me.candiesjar.fallbackserver.bungee.enums;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;

import java.util.List;

public enum ConfigFields {

    PERMISSION("Fallback_Server.command_permission"),
    TAB_COMPLETE("Fallback_Server.command_tab_complete"),
    COMMAND_WITHOUT_PERMISSION("Fallback_Server.command_without_permission"),
    STATS("Fallback_Server.use_stats"),
    UPDATE_CHECKER("Fallback_Server.check_updates"),

    USE_HUB_COMMAND("Hub.commands.enable_command"),
    HUB_COMMANDS("Hub.commands.command_aliases"),
    LOBBIES("Hub.lobbies.server_list"),
    DISABLE_SERVERS("Hub.enable_disabled_servers");

    private final String path;

    ConfigFields(String path) {
        this.path = path;
    }

    public boolean getBoolean() {
        return FallbackServerBungee.getInstance().getConfig().getBoolean(path);
    }

    public String getString() {
        return FallbackServerBungee.getInstance().getConfig().getString(path);
    }

    public List<String> getStringList() {
        return FallbackServerBungee.getInstance().getConfig().getStringList(path);
    }

}
