package me.candiesjar.fallbackserver.enums;

import com.velocitypowered.api.command.CommandSource;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import me.candiesjar.fallbackserver.utils.player.ChatUtil;
import net.kyori.adventure.text.Component;

public enum VelocityMessages {

    PREFIX("MESSAGES.prefix"),
    CORRECT_SYNTAX("MESSAGES.syntax"),
    RELOAD("MESSAGES.reloaded"),
    PLAYER_ONLY("MESSAGES.only_player"),
    ALREADY_IN_LOBBY("MESSAGES.already_in_hub"),
    MOVED_TO_HUB("MESSAGES.moved"),
    KICKED_TO_LOBBY("MESSAGES.moved_to_lobby"),
    BLOCKED_COMMAND("MESSAGES.disabled_command"),
    NEW_UPDATE("MESSAGES.new_update"),
    MAIN_COMMAND("MESSAGES.fallback_command"),
    NO_PERMISSION("MESSAGES.no_permission"),
    NO_SERVER("MESSAGES.no_server"),

    EMPTY_SERVER("MESSAGES.empty_server"),
    SERVER_CONTAINED("MESSAGES.server_is_added"),
    UNAVAILABLE_SERVER("MESSAGES.server_not_available"),
    SERVER_ADDED("MESSAGES.server_added"),

    CONNECTION_FAILED("MESSAGES.connection_failed"),

    STATS_COMMAND("MESSAGES.stats_command"),

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
    CONNECTED_TITLE("TITLES.reconnect.connected_title"),
    CONNECTED_SUB_TITLE("TITLES.reconnect.connected_sub_title"),

    USE_HUB_TITLE("TITLES.lobby.enabled"),
    HUB_TITLE_FADE_IN("TITLES.lobby.fade_in"),
    HUB_TITLE_FADE_OUT("TITLES.lobby.fade_out"),
    HUB_TITLE_STAY("TITLES.lobby.stay"),
    HUB_TITLE("TITLES.lobby.lobby_title"),
    HUB_SUB_TITLE("TITLES.lobby.lobby_sub_title"),
    HUB_TITLE_DELAY("TITLES.lobby.delay");

    private final String path;
    private static final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    VelocityMessages(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getMessages().getConfig().get(path));
    }

    public void send(CommandSource commandSource, Placeholder... placeholders) {

        if (ChatUtil.getString(this).equals("")) {
            return;
        }

        commandSource.sendMessage(Component.text(ChatUtil.getFormattedString(this, placeholders)));
    }

    public void sendList(CommandSource commandSource, Placeholder... placeHolder) {
        ChatUtil.sendFormattedList(this, commandSource, placeHolder);
    }

}
