package me.candiesjar.fallbackserver.config;

import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.md_5.bungee.api.CommandSender;

@Getter
public enum BungeeMessages {

    PREFIX("MESSAGES.prefix"),
    CORRECT_SYNTAX("MESSAGES.syntax"),
    RELOAD("MESSAGES.reloaded"),
    ONLY_PLAYER("MESSAGES.only_player"),
    ALREADY_IN_LOBBY("MESSAGES.already_in_hub"),
    MOVED_TO_HUB("MESSAGES.moved"),
    KICKED_TO_LOBBY("MESSAGES.moved_to_lobby"),
    BLOCKED_COMMAND("MESSAGES.disabled_command"),
    NEW_UPDATE("MESSAGES.new_update"),
    MAIN_COMMAND("MESSAGES.fallback_command"),
    NO_PERMISSION("MESSAGES.no_permission"),
    NO_SERVER("MESSAGES.no_server"),
    EXITING_RECONNECT("MESSAGES.exiting_reconnect"),
    ERRORS_FOUND("MESSAGES.errors_found"),

    EMPTY_SERVER("MESSAGES.empty_server"),
    SERVER_CONTAINED("MESSAGES.server_is_added"),
    SERVER_REMOVED("MESSAGES.server_removed"),
    SERVER_NOT_CONTAINED("MESSAGES.server_not_added"),
    UNAVAILABLE_SERVER("MESSAGES.server_not_available"),
    SERVER_ADDED("MESSAGES.server_added"),
    CONNECTION_FAILED("MESSAGES.connection_failed"),

    STATS_COMMAND("MESSAGES.stats_command"),

    SERVERS_COMMAND_ONLINE("MESSAGES.servers_command.online"),
    SERVERS_COMMAND_OFFLINE("MESSAGES.servers_command.offline"),
    SERVERS_COMMAND_LIST("MESSAGES.servers_command.servers"),
    SERVERS_COMMAND_GROUP("MESSAGES.servers_command.group"),
    SERVERS_COMMAND_MAINTENANCE("MESSAGES.servers_command.maintenance"),
    SERVERS_COMMAND_HEADER("MESSAGES.servers_command.header"),
    SERVERS_COMMAND_FOOTER("MESSAGES.servers_command.footer"),

    USE_FALLBACK_TITLE("TITLES.fallback.enabled"),
    FALLBACK_DELAY("TITLES.fallback.delay"),
    FALLBACK_FADE_IN("TITLES.fallback.fade_in"),
    FALLBACK_FADE_OUT("TITLES.fallback.fade_out"),
    FALLBACK_STAY("TITLES.fallback.stay"),
    FALLBACK_TITLE("TITLES.fallback.fallback_title"),
    FALLBACK_SUB_TITLE("TITLES.fallback.fallback_sub_title"),

    RECONNECT_TITLE("TITLES.reconnect.reconnect_title"),
    RECONNECT_SUB_TITLE("TITLES.reconnect.reconnect_sub_title"),
    CONNECTING_TITLE("TITLES.reconnect.connecting_title"),
    CONNECTING_SUB_TITLE("TITLES.reconnect.connecting_sub_title"),
    CONNECTED_TITLE("TITLES.reconnect.connected.title"),
    CONNECTED_SUB_TITLE("TITLES.reconnect.connected.sub_title"),
    RECONNECT_TITLE_PULSE("TITLES.reconnect.title_beat"),
    CONNECTED_DELAY("TITLES.reconnect.connected.delay"),
    CONNECTED_FADE_IN("TITLES.reconnect.connected.fade_in"),
    CONNECTED_FADE_OUT("TITLES.reconnect.connected.fade_out"),
    CONNECTED_STAY("TITLES.reconnect.connected.stay"),

    USE_ALREADY_IN_LOBBY_TITLE("TITLES.already_in_lobby.enabled"),
    ALREADY_IN_LOBBY_TITLE("TITLES.already_in_lobby.title"),
    ALREADY_IN_LOBBY_SUB_TITLE("TITLES.already_in_lobby.sub_title"),
    ALREADY_IN_LOBBY_FADE_IN("TITLES.already_in_lobby.fade_in"),
    ALREADY_IN_LOBBY_FADE_OUT("TITLES.already_in_lobby.fade_out"),
    ALREADY_IN_LOBBY_STAY("TITLES.already_in_lobby.stay"),

    USE_HUB_TITLE("TITLES.lobby.enabled"),
    HUB_TITLE_FADE_IN("TITLES.lobby.fade_in"),
    HUB_TITLE_FADE_OUT("TITLES.lobby.fade_out"),
    HUB_TITLE_STAY("TITLES.lobby.stay"),
    HUB_TITLE("TITLES.lobby.title"),
    HUB_SUB_TITLE("TITLES.lobby.sub_title");

    private final String path;

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();

    BungeeMessages(String path) {
        this.path = path;
    }

    public void send(CommandSender commandSender, Placeholder... placeholders) {

        if (ChatUtil.getString(this).isEmpty()) {
            return;
        }

        commandSender.sendMessage(ChatUtil.asComponent(ChatUtil.getFormattedString(this, placeholders).replace("%prefix%", ChatUtil.getFormattedString(PREFIX))));
    }

    public void sendList(CommandSender commandSender, Placeholder... placeHolder) {
        ChatUtil.sendFormattedList(this, commandSender, placeHolder);
    }

    public String getString() {
        return fallbackServerBungee.getMessagesConfig().getString(path);
    }

    public int getInt() {
        return fallbackServerBungee.getMessagesConfig().getInt(path);
    }

    public boolean getBoolean() {
        return fallbackServerBungee.getMessagesConfig().getBoolean(path);
    }

}
