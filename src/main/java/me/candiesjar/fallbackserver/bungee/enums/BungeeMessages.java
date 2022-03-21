package me.candiesjar.fallbackserver.bungee.enums;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import net.md_5.bungee.api.ChatColor;

import java.util.List;

public enum BungeeMessages {

    PREFIX("MESSAGES.prefix"),
    PARAMETERS("MESSAGES.correct_syntax"),
    RELOAD("MESSAGES.reload_message"),
    ONLY_PLAYER("MESSAGES.only_player"),
    ALREADY_IN_HUB("MESSAGES.already_in_hub"),
    CONNECT_TO_HUB("MESSAGES.connecting_to_hub"),
    CONNECTED("MESSAGES.server_crash"),
    BLOCKED_COMMAND("MESSAGES.disabled_command"),
    NEW_UPDATE("MESSAGES.new_update"),
    MAIN_COMMAND("MESSAGES.fallback_command"),
    NO_PERMISSION("MESSAGES.missing_permission"),
    NO_SERVER("MESSAGES.no_server"),

    EMPTY_SERVER("MESSAGES.empty_server"),
    SERVER_IS_ADDED("MESSAGES.server_is_added"),
    SERVER_NOT_AVAILABLE("MESSAGES.server_not_available"),
    SERVER_ADDED("MESSAGES.server_added"),

    CONFIGURATION_WARN("MESSAGES.config_warn"),
    CONFIGURATION_RESTORED("MESSAGES.configuration_restored"),
    MESSAGES_WARN("MESSAGES.messages_warn"),
    MESSAGES_RESTORED("MESSAGES.messages_restored"),

    USE_FALLBACK_TITLE("TITLES.fallback.enabled"),
    FALLBACK_FADE_IN("TITLES.fallback.fade_in"),
    FALLBACK_FADE_OUT("TITLES.fallback.fade_out"),
    FALLBACK_STAY("TITLES.fallback.stay"),
    FALLBACK_TITLE("TITLES.fallback.fallback_title"),
    FALLBACK_SUB_TITLE("TITLES.fallback.fallback_sub_title"),

    USE_HUB_TITLE("TITLES.lobby.enabled"),
    HUB_TITLE_FADE_IN("TITLES.lobby.fade_in"),
    HUB_TITLE_FADE_OUT("TITLES.lobby.fade_out"),
    HUB_TITLE_STAY("TITLES.lobby.stay"),
    HUB_TITLE("TITLES.lobby.lobby_title"),
    HUB_SUB_TITLE("TITLES.lobby.lobby_sub_title");

    private final String path;

    BungeeMessages(String path) {
        this.path = path;
    }

    public String getString() {
        return FallbackServerBungee.getInstance().getMessagesConfig().getString(path);
    }

    public String getFormattedString() {
        return ChatColor.translateAlternateColorCodes('&', FallbackServerBungee.getInstance().getMessagesConfig().getString(path));
    }
    public static String getFormattedString(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public List<String> getStringList() {
        return FallbackServerBungee.getInstance().getMessagesConfig().getStringList(path);
    }

    public int getInt() {
        return FallbackServerBungee.getInstance().getMessagesConfig().getInt(path);
    }

    public boolean getBoolean() {
        return FallbackServerBungee.getInstance().getMessagesConfig().getBoolean(path);
    }
}
