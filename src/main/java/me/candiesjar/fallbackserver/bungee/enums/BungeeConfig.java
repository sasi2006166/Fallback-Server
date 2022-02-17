package me.candiesjar.fallbackserver.bungee.enums;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;

import java.util.List;

public enum BungeeConfig {

    PERMISSION("Fallback_Server.command_permission"),
    RELOAD_PERMISSION("Fallback_Server.reload_permission"),
    TAB_COMPLETE("Fallback_Server.command_tab_complete"),
    COMMAND_WITHOUT_PERMISSION("Fallback_Server.command_without_permission"),
    UPDATE_CHECKER("Fallback_Server.check_updates"),

    USE_HUB_COMMAND("Hub.commands.enable_command"),
    HUB_COMMANDS("Hub.commands.command_aliases"),
    LOBBIES("Hub.server_list"),
    DISABLE_SERVERS("Hub.enable_disabled_servers");

    private final String path;

    BungeeConfig(String path) {
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
