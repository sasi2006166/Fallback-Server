package me.candiesjar.fallbackserver.enums;

import com.velocitypowered.api.command.CommandSource;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

public enum VelocityMessages {

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

    USE_FALLBACK_TITLE("TITLES.fallback.enabled"),
    FALLBACK_TITLE("TITLES.fallback.fallback_title"),
    FALLBACK_SUB_TITLE("TITLES.fallback.fallback_sub_title"),
    FALLBACK_FADE_IN("TITLES.fallback.fade_in"),
    FALLBACK_FADE_OUT("TITLES.fallback.fade_out"),
    FALLBACK_STAY("TITLES.fallback.stay"),

    USE_HUB_TITLE("TITLES.lobby.enabled"),
    HUB_TITLE("TITLES.lobby.lobby_title"),
    HUB_SUB_TITLE("TITLES.lobby.lobby_sub_title"),
    HUB_TITLE_FADE_IN("TITLES.lobby.fade_in"),
    HUB_TITLE_FADE_OUT("TITLES.lobby.fade_out"),
    HUB_TITLE_STAY("TITLES.lobby.stay");

    private final String path;
    private static final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    VelocityMessages(String path) {
        this.path = path;
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(instance.getMessagesTextFile().getConfig().get(path));
    }

    public List<String> getStringList() {
        return instance.getMessagesTextFile().getConfig().getStringList(path);
    }

    public static void sendList(CommandSource commandSource, List<String> list) {
        for (String s : list) {
            commandSource.sendMessage(colorize(s));
        }
    }

    public String color() {
        return get(String.class).replace("&", "§");
    }

    public static TextComponent colorize(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
