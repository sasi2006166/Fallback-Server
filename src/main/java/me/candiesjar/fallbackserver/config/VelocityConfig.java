package me.candiesjar.fallbackserver.config;

import me.candiesjar.fallbackserver.FallbackServerVelocity;

import java.util.List;

public enum VelocityConfig {
    DEBUG("settings.debug"),
    TAB_COMPLETE("settings.command_tab_complete"),
    PING_MODE("settings.ping_mode"),
    HIDE_COMMAND("settings.hide_command"),
    CLEAR_CHAT_FALLBACK("settings.clear_chat.fallback"),
    CLEAR_CHAT_SERVER_SWITCH("settings.clear_chat.server_switch"),
    CLEAR_CHAT_RECONNECT("settings.clear_chat.reconnect"),
    CLEAR_CHAT_RECONNECT_JOIN("settings.clear_chat.reconnect_join"),

    RECONNECT_MAX_TRIES("settings.auto_reconnect.max_tries"),
    RECONNECT_TASK_DELAY("settings.auto_reconnect.ping_delay"),
    RECONNECT_PLAYER_COUNT_CHECK("settings.auto_reconnect.player_count_check"),
    RECONNECT_IGNORED_SERVERS("settings.auto_reconnect.ignored_servers"),
    RECONNECT_USE_FALLBACK("settings.auto_reconnect.player_sort"),
    RECONNECT_IGNORED_REASONS("settings.auto_reconnect.ignored_reasons"),
    RECONNECT_JOIN_LIMBO("settings.auto_reconnect.join_limbo"),

    RECONNECT_USE_PHYSICAL("settings.auto_reconnect.physical_reconnect.enabled"),
    RECONNECT_PHYSICAL_SERVER("settings.auto_reconnect.physical_reconnect.server"),

    RECONNECT_LIMBO_NAME("settings.auto_reconnect.limbo_settings.name"),
    RECONNECT_LIMBO_DIMENSION("settings.auto_reconnect.limbo_settings.dimension"),
    RECONNECT_LIMBO_WORLD_TIME("settings.auto_reconnect.limbo_settings.world_time"),
    RECONNECT_LIMBO_GAMEMODE("settings.auto_reconnect.limbo_settings.gamemode"),

    RECONNECT_USE_SCHEMATIC("settings.auto_reconnect.limbo_settings.schematic.enabled"),
    RECONNECT_SCHEMATIC_NAME("settings.auto_reconnect.limbo_settings.schematic.name"),
    RECONNECT_SCHEMATIC_X("settings.auto_reconnect.limbo_settings.schematic.x"),
    RECONNECT_SCHEMATIC_Y("settings.auto_reconnect.limbo_settings.schematic.y"),
    RECONNECT_SCHEMATIC_Z("settings.auto_reconnect.limbo_settings.schematic.z"),

    RECONNECT_LIMBO_X("settings.auto_reconnect.limbo_settings.x"),
    RECONNECT_LIMBO_Y("settings.auto_reconnect.limbo_settings.y"),
    RECONNECT_LIMBO_Z("settings.auto_reconnect.limbo_settings.z"),
    RECONNECT_LIMBO_YAW("settings.auto_reconnect.limbo_settings.yaw"),
    RECONNECT_LIMBO_PITCH("settings.auto_reconnect.limbo_settings.pitch"),

    RECONNECT_PING_TIMEOUT("settings.auto_reconnect.ping_timeout"),
    RECONNECT_CLEAR_TABLIST("settings.auto_reconnect.clear_tab-list"),
    RECONNECT_TITLE("settings.auto_reconnect.title.enable"),
    RECONNECT_TITLE_MODE("settings.auto_reconnect.title.mode"),

    UPDATER("settings.updater"),
    PING_DELAY("settings.ping_delay"),
    PING_TIMEOUT("settings.ping_timeout"),
    TELEMETRY("settings.telemetry"),
    USE_COMMAND_BLOCKER("settings.command_blocker"),
    FALLBACK("settings.fallback"),
    IGNORED_REASONS("settings.ignored_reasons"),
    ADMIN_NOTIFICATION("settings.admin_notification"),
    USE_IGNORED_SERVERS("settings.ignored_servers"),
    IGNORED_SERVERS_LIST("settings.ignored_servers_list"),
    JOIN_BALANCING("settings.join_balancing"),
    JOIN_BALANCING_GROUP("settings.join_balancing_group"),

    ADMIN_PERMISSION("sub_commands.admin.permission"),
    RELOAD_PERMISSION("sub_commands.reload.permission"),
    DEBUG_PERMISSION("sub_commands.debug.permission"),

    LOBBY_COMMAND("settings.lobby_command"),
    LOBBY_ALIASES("settings.lobby_command_aliases"),

    CREATE_COMMAND("sub_commands.create.enabled"),
    CREATE_COMMAND_PERMISSION("sub_commands.create.permission"),

    ADD_COMMAND("sub_commands.add.enabled"),
    ADD_COMMAND_PERMISSION("sub_commands.add.permission"),

    REMOVE_COMMAND("sub_commands.remove.enabled"),
    REMOVE_COMMAND_PERMISSION("sub_commands.remove.permission"),

    STATUS_COMMAND("sub_commands.status.enabled"),
    STATUS_COMMAND_PERMISSION("sub_commands.status.permission"),

    SERVERS_COMMAND("sub_commands.servers.enabled"),
    SERVERS_COMMAND_PERMISSION("sub_commands.servers.permission");

    private final String path;
    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();

    VelocityConfig(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(fallbackServerVelocity.getConfigTextFile().getConfig().get(path));
    }

    public List<String> getStringList() {
        return fallbackServerVelocity.getConfigTextFile().getConfig().getStringList(path);
    }
}
