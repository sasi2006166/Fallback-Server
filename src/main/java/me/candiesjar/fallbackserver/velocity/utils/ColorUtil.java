package me.candiesjar.fallbackserver.velocity.utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtil {

    public static TextComponent colorize(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
