package me.candiesjar.fallbackserver.enums;

import com.velocitypowered.api.command.CommandSource;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import me.candiesjar.fallbackserver.utils.chat.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public enum VelocityMessages {

    PREFIX("MESSAGES.prefix"),
    PARAMETERS("MESSAGES.correct_syntax"),
    RELOAD("MESSAGES.reload_message"),
    PLAYER_ONLY("MESSAGES.only_player"),
    ALREADY_IN_LOBBY("MESSAGES.already_in_hub"),
    MOVED_TO_HUB("MESSAGES.connecting_to_hub"),
    KICKED_TO_LOBBY("MESSAGES.moved_to_lobby"),
    BLOCKED_COMMAND("MESSAGES.disabled_command"),
    NEW_UPDATE("MESSAGES.new_update"),
    MAIN_COMMAND("MESSAGES.fallback_command"),
    NO_PERMISSION("MESSAGES.missing_permission"),
    NO_SERVER("MESSAGES.no_server"),

    EMPTY_SERVER("MESSAGES.empty_server"),
    SERVER_CONTAINED("MESSAGES.server_is_added"),
    UNAVAILABLE_SERVER("MESSAGES.server_not_available"),
    SERVER_ADDED("MESSAGES.server_added"),

    USE_FALLBACK_TITLE("TITLES.fallback.enabled"),
    FALLBACK_TITLE_DELAY("TITLES.fallback.delay"),
    FALLBACK_FADE_IN("TITLES.fallback.fade_in"),
    FALLBACK_FADE_OUT("TITLES.fallback.fade_out"),
    FALLBACK_STAY("TITLES.fallback.stay"),
    FALLBACK_TITLE("TITLES.fallback.fallback_title"),
    FALLBACK_SUB_TITLE("TITLES.fallback.fallback_sub_title"),

    USE_HUB_TITLE("TITLES.lobby.enabled"),
    HUB_TITLE_DELAY("TITLES.lobby.delay"),
    HUB_TITLE_FADE_IN("TITLES.lobby.fade_in"),
    HUB_TITLE_FADE_OUT("TITLES.lobby.fade_out"),
    HUB_TITLE_STAY("TITLES.lobby.stay"),
    HUB_TITLE("TITLES.lobby.lobby_title"),
    HUB_SUB_TITLE("TITLES.lobby.lobby_sub_title");

    private final String path;
    private static final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    VelocityMessages(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getMessagesTextFile().getConfig().get(path));
    }

    public void send(CommandSource commandSource, PlaceHolder... placeHolders) {

        if (ChatUtil.getString(this).equals("")) {
            return;
        }

        commandSource.sendMessage(Component.text(ChatUtil.getFormattedString(this, placeHolders)));
    }

    public void sendList(CommandSource commandSource, PlaceHolder... placeHolder) {
        ChatUtil.sendFormattedList(this, commandSource, placeHolder);
    }


    public String color() {
        return get(String.class).replace("&", "§");
    }

    public static TextComponent color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
