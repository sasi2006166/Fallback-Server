package me.candiesjar.fallbackserver.velocity.enums;

import me.candiesjar.fallbackserver.velocity.utils.ConfigurationUtil;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

public enum VelocityMessages {

    NOT_PLAYER("Messages.not_player");

    private final String path;

    VelocityMessages(String path) {
        this.path = path;
    }

    public String getString() {
        return ConfigurationUtil.getConfig().getString(path);
    }

    public List<String> getStringList() {
        return ConfigurationUtil.getConfig().getStringList(path);
    }

    public static TextComponent colorize(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
