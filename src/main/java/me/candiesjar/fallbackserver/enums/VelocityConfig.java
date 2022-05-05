package me.candiesjar.fallbackserver.enums;

import me.candiesjar.fallbackserver.FallbackServerVelocity;

import java.util.List;

public enum VelocityConfig {

    TAB_COMPLETE("settings.command_tab_complete"),
    COMMAND_WITHOUT_PERMISSION("settings.command_without_permission"),
    UPDATE_CHECKER("settings.check_updates"),
    TASK_PERIOD("settings.task_period"),
    USE_STATS("settings.stats"),
    LOBBIES("settings.fallback_list"),
    BLACKLISTED_WORDS("settings.blacklisted_words"),

    ADMIN_PERMISSION("sub_commands.admin.permission"),
    RELOAD_PERMISSION("sub_commands.reload.permission"),

    ADD_COMMAND("sub_commands.add.enabled"),
    ADD_COMMAND_PERMISSION("sub_commands.add.permission"),

    REMOVE_COMMAND("sub_commands.remove.enabled"),
    REMOVE_COMMAND_PERMISSION("sub_commands.remove.permission"),

    RESET_COMMAND("sub_commands.reset.enabled"),
    RESET_COMMAND_PERMISSION("sub_commands.permissions.reset_permission"),

    STATUS_COMMAND("sub_commands.status.enabled"),
    STATUS_COMMAND_PERMISSION("sub_commands.status.permission"),

    SET_COMMAND("sub_commands.set.enabled"),
    SET_COMMAND_PERMISSION("sub_commands.set.permission"),

    BLACKLISTED_SERVERS("settings.server_blacklist"),
    BLACKLISTED_SERVERS_LIST("settings.server_blacklist_list"),

    LOBBY_COMMAND("settings.lobby_command"),
    LOBBY_ALIASES("settings.lobby_command_aliases"),

    DISABLED_SERVERS("settings.disabled_servers");


    private static final FallbackServerVelocity inst = FallbackServerVelocity.getInstance();
    private final String path;

    VelocityConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(inst.getConfigTextFile().getConfig().get(path));
    }

    public List<String> getStringList() {
        return inst.getConfigTextFile().getConfig().getStringList(path);
    }
}
