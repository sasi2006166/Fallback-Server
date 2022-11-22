package me.candiesjar.fallbackserver.enums;

import me.candiesjar.fallbackserver.FallbackServerVelocity;

import java.util.List;

public enum VelocityConfig {
    TAB_COMPLETION("settings.command_tab_complete"),
    HIDE_COMMAND("settings.hide_command"),
    FALLBACK_MODE("settings.fallback_mode"),
    DEBUG_MODE("settings.debug"),

    RECONNECT_TRIES("settings.auto_reconnect.max_tries"),
    RECONNECT_DELAY("settings.auto_reconnect.ping_delay"),
    RECONNECT_KICK("settings.auto_reconnect.disconnect"),
    RECONNECT_SORT("settings.auto_reconnect.player_sort"),

    RECONNECT_TITLE("settings.auto_reconnect.title.enable"),
    RECONNECT_TITLE_MODE("settings.auto_reconnect.title.mode"),

    UPDATER("settings.updater"),
    PING_DELAY("settings.ping_delay"),
    TELEMETRY("settings.stats"),
    USE_COMMAND_BLOCKER("settings.use_command_blocker"),
    LOBBIES_LIST("settings.fallback_list"),
    IGNORED_REASONS("settings.blacklisted_words"),
    ADMIN_NOTIFICATION("settings.admin_notification"),
    USE_BLACKLISTED_SERVERS("settings.server_blacklist"),
    BLACKLISTED_SERVERS_LIST("settings.server_blacklist_list"),
    JOIN_BALANCING("settings.join_balancing"),

    ADMIN_PERMISSION("sub_commands.admin.permission"),
    RELOAD_PERMISSION("sub_commands.reload.permission"),

    ADD_COMMAND("sub_commands.add.enabled"),
    ADD_COMMAND_PERMISSION("sub_commands.add.permission"),

    REMOVE_COMMAND("sub_commands.remove.enabled"),
    REMOVE_COMMAND_PERMISSION("sub_commands.remove.permission"),

    STATUS_COMMAND("sub_commands.status.enabled"),
    STATUS_COMMAND_PERMISSION("sub_commands.status.permission"),

    SET_COMMAND("sub_commands.set.enabled"),
    SET_COMMAND_PERMISSION("sub_commands.set.permission"),

    LOBBY_COMMAND("settings.lobby_command"),
    LOBBY_ALIASES("settings.lobby_command_aliases"),

    UPDATE_COMMAND("sub_commands.update.enabled"),
    UPDATE_COMMAND_PERMISSION("sub_commands.update.permission");

    private static final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();
    private final String configurationPath;

    VelocityConfig(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getConfigTextFile().getConfig().get(configurationPath));
    }

    public List<String> getStringList() {
        return instance.getConfigTextFile().getConfig().getStringList(configurationPath);
    }
}
