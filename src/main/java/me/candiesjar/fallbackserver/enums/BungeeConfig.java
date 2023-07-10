package me.candiesjar.fallbackserver.enums;

import me.candiesjar.fallbackserver.FallbackServerBungee;

import java.util.List;

public enum BungeeConfig {

    TAB_COMPLETION("settings.command_tab_complete"),
    HIDE_COMMAND("settings.hide_command"),
    FALLBACK_MODE("settings.fallback_mode"),
    CLEAR_CHAT_RECONNECT("settings.clear_chat.reconnect"),
    CLEAR_CHAT_FALLBACK("settings.clear_chat.fallback"),
    DEBUG_MODE("settings.debug"),

    RECONNECT_TRIES("settings.auto_reconnect.max_tries"),
    RECONNECT_IGNORED_REASONS("settings.auto_reconnect.ignored_reasons"),
    RECONNECT_DELAY("settings.auto_reconnect.ping_delay"),
    RECONNECT_PING_THRESHOLD("settings.auto_reconnect.ping_threshold"),
    RECONNECT_CONNECTION_DELAY("settings.auto_reconnect.connection_delay"),
    RECONNECT_SORT("settings.auto_reconnect.player_sort"),
    RECONNECT_TASK_DELAY("settings.auto_reconnect.task_delay"),
    RECONNECT_IGNORED_SERVERS("settings.auto_reconnect.ignored_servers"),
    RECONNECT_TITLE("settings.auto_reconnect.title.enable"),
    RECONNECT_TITLE_MODE("settings.auto_reconnect.title.mode"),

    UPDATER("settings.updater"),
    PING_DELAY("settings.ping_delay"),
    TELEMETRY("settings.stats"),
    USE_COMMAND_BLOCKER("settings.use_command_blocker"),
    LOBBIES_LIST("settings.fallback_list"),
    IGNORED_REASONS("settings.ignored_reasons"),
    ADMIN_NOTIFICATION("settings.admin_notification"),
    USE_IGNORED_SERVERS("settings.use_ignored_servers"),
    BLACKLISTED_SERVERS_LIST("settings.ignored_servers_list"),
    JOIN_BALANCING("settings.join_balancing"),

    ADMIN_PERMISSION("sub_commands.admin.permission"),
    RELOAD_PERMISSION("sub_commands.reload.permission"),

    DEBUG_COMMAND_PERMISSION("sub_commands.debug.permission"),

    ADD_COMMAND("sub_commands.add.enabled"),
    ADD_COMMAND_PERMISSION("sub_commands.add.permission"),

    REMOVE_COMMAND("sub_commands.remove.enabled"),
    REMOVE_COMMAND_PERMISSION("sub_commands.remove.permission"),

    STATUS_COMMAND("sub_commands.status.enabled"),
    STATUS_COMMAND_PERMISSION("sub_commands.status.permission"),

    LOBBY_COMMAND("settings.lobby_command"),
    LOBBY_ALIASES("settings.lobby_command_aliases"),

    UPDATE_COMMAND("sub_commands.update.enabled"),
    UPDATE_COMMAND_PERMISSION("sub_commands.update.permission");

    private final String path;
    private static final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    BungeeConfig(String path) {
        this.path = path;
    }

    public int getInt() {
        return instance.getConfig().getInt(path);
    }

    public boolean getBoolean() {
        return instance.getConfig().getBoolean(path);
    }

    public String getString() {
        return instance.getConfig().getString(path);
    }

    public List<String> getStringList() {
        return instance.getConfig().getStringList(path);
    }

}
