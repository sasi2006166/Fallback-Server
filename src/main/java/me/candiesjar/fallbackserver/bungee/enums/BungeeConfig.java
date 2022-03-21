package me.candiesjar.fallbackserver.bungee.enums;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;

import java.util.List;

public enum BungeeConfig {

    TAB_COMPLETE("settings.command_tab_complete"),
    COMMAND_WITHOUT_PERMISSION("settings.command_without_permission"),
    UPDATE_CHECKER("settings.check_updates"),
    TASK_PERIOD("settings.task_period"),

    ADMIN_PERMISSION("commands.permissions.command_permission"),
    RELOAD_COMMAND_PERMISSION("commands.permissions.reload_permission"),
    ADD_COMMAND_PERMISSION("commands.permissions.add_permission"),
    RESET_COMMAND_PERMISSION("commands.permissions.reset_permission"),

    USE_HUB_COMMAND("Hub.commands.enable_command"),
    HUB_COMMANDS("Hub.commands.command_aliases"),
    LOBBIES("Hub.server_list"),
    DISABLED_SERVERS("Hub.enable_disabled_servers");

    private final String path;

    BungeeConfig(String path) {
        this.path = path;
    }

    public int getInt() {
        return FallbackServerBungee.getInstance().getConfig().getInt(path);
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
