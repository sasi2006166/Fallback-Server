package me.candiesjar.fallbackserver.enums;

import me.candiesjar.fallbackserver.FallbackServerBungee;

import java.util.List;

public enum BungeeConfig {

    TAB_COMPLETE("settings.command_tab_complete"),
    PING_STRATEGY("settings.ping_mode"),
    CLEAR_CHAT_RECONNECT("settings.clear_chat.reconnect"),
    CLEAR_CHAT_SERVER_SWITCH("settings.clear_chat.server_switch"),
    CLEAR_CHAT_FALLBACK("settings.clear_chat.fallback"),
    CLEAR_CHAT_RECONNECT_JOIN("settings.clear_chat.reconnect_join"),
    USE_DEBUG("settings.debug"),

    RECONNECT_TRIES("settings.auto_reconnect.max_tries"),
    RECONNECT_IGNORED_REASONS("settings.auto_reconnect.ignored_reasons"),
    RECONNECT_DELAY("settings.auto_reconnect.ping_delay"),
    RECONNECT_PLAYER_COUNT_CHECK("settings.auto_reconnect.player_count_check"),
    RECONNECT_PING_THRESHOLD("settings.auto_reconnect.ping_threshold"),
    RECONNECT_CONNECTION_DELAY("settings.auto_reconnect.connection_delay"),
    RECONNECT_SORT("settings.auto_reconnect.player_sort"),
    RECONNECT_TASK_DELAY("settings.auto_reconnect.task_delay"),
    RECONNECT_IGNORED_SERVERS("settings.auto_reconnect.ignored_servers"),
    RECONNECT_CLEAR_TABLIST("settings.auto_reconnect.clear_tab-list"),
    RECONNECT_USE_SERVER("settings.auto_reconnect.physical_reconnect.enabled"),
    RECONNECT_SERVER("settings.auto_reconnect.physical_reconnect.server"),
    RECONNECT_TITLE("settings.auto_reconnect.title.enable"),
    RECONNECT_TITLE_MODE("settings.auto_reconnect.title.mode"),

    UPDATER("settings.updater"),
    PING_DELAY("settings.ping_delay"),
    TELEMETRY("settings.telemetry"),
    USE_COMMAND_BLOCKER("settings.command_blocker"),
    FALLBACK_SECTION("settings.fallback"),
    IGNORED_REASONS("settings.ignored_reasons"),
    ADMIN_NOTIFICATION("settings.admin_notification"),
    USE_IGNORED_SERVERS("settings.ignored_servers"),
    IGNORED_SERVER_LIST("settings.ignored_servers_list"),
    JOIN_BALANCING("settings.join_balancing"),
    JOIN_BALANCING_GROUP("settings.join_balancing_group"),

    ADMIN_PERMISSION("sub_commands.admin.permission"),
    RELOAD_PERMISSION("sub_commands.reload.permission"),

    DEBUG_COMMAND_PERMISSION("sub_commands.debug.permission"),

    ADD_COMMAND("sub_commands.add.enabled"),
    ADD_COMMAND_PERMISSION("sub_commands.add.permission"),

    REMOVE_COMMAND("sub_commands.remove.enabled"),
    REMOVE_COMMAND_PERMISSION("sub_commands.remove.permission"),

    STATUS_COMMAND("sub_commands.status.enabled"),
    STATUS_COMMAND_PERMISSION("sub_commands.status.permission"),

    SERVERS_COMMAND("sub_commands.servers.enabled"),
    SERVERS_COMMAND_PERMISSION("sub_commands.servers.permission"),

    LOBBY_COMMAND("settings.lobby_command"),
    LOBBY_ALIASES("settings.lobby_command_aliases");

    private final String path;
    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();

    BungeeConfig(String path) {
        this.path = path;
    }

    public int getInt() {
        return fallbackServerBungee.getConfig().getInt(path);
    }

    public boolean getBoolean() {
        return fallbackServerBungee.getConfig().getBoolean(path);
    }

    public String getString() {
        return fallbackServerBungee.getConfig().getString(path);
    }

    public List<String> getStringList() {
        return fallbackServerBungee.getConfig().getStringList(path);
    }

}
