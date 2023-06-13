package me.candiesjar.fallbackserver.enums;

import me.candiesjar.fallbackserver.FallbackServerVelocity;

import java.util.List;

public enum VelocityConfig {
    TAB_COMPLETE("settings.command_tab_complete"),
    HIDE_COMMAND("settings.hide_command"),
    FALLBACK_MODE("settings.fallback_mode"),
    CLEAR_CHAT_RECONNECT("settings.clear_chat.reconnect"),
    CLEAR_CHAT_FALLBACK("settings.clear_chat.fallback"),
    DEBUG_MODE("settings.debug"),

    RECONNECT_USE_SOCKETS("settings.auto_reconnect.use_sockets"),
    RECONNECT_TRIES("settings.auto_reconnect.max_tries"),
    RECONNECT_DELAY("settings.auto_reconnect.ping_delay"),
    RECONNECT_SOCKET_PORT("settings.auto_reconnect.socket_port"),
    RECONNECT_FALLBACK("settings.auto_reconnect.player_sort"),
    RECONNECT_SOCKET_TASK("settings.auto_reconnect.socket_task"),
    RECONNECT_IGNORED_REASONS("settings.auto_reconnect.ignored_reasons"),
    RECONNECT_JOIN_LIMBO("settings.auto_reconnect.join_limbo"),
    RECONNECT_PING_TIMEOUT("settings.auto_reconnect.ping_timeout"),

    RECONNECT_TITLE("settings.auto_reconnect.title.enable"),
    RECONNECT_TITLE_MODE("settings.auto_reconnect.title.mode"),

    UPDATER("settings.updater"),
    PING_DELAY("settings.ping_delay"),
    TELEMETRY("settings.stats"),
    USE_COMMAND_BLOCKER("settings.command_blocker"),
    LOBBIES_LIST("settings.fallback_list"),
    IGNORED_REASONS("settings.ignored_reasons"),
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
        return clazz.cast(instance.getConfig().getConfig().get(configurationPath));
    }

    public List<String> getStringList() {
        return instance.getConfig().getConfig().getStringList(configurationPath);
    }
}
