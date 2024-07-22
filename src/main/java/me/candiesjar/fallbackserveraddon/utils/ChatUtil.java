package me.candiesjar.fallbackserveraddon.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ChatUtil {

    private final FallbackServerAddon instance = FallbackServerAddon.getInstance();

    public String color(Player player, String message) {
        return convertHexColors(applyPlaceholder(player, message)).replace("&", "§");
    }

    private String convertHexColors(String message) {
        if (!containsHexColor(message)) {
            return message;
        }
        Pattern hexPattern = Pattern.compile("(#[A-Fa-f0-9]{6}|<#[A-Fa-f0-9]{6}>|&#[A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String hexCode = matcher.group();
            String colorCode = hexCode.substring(1, 7);
            if (hexCode.startsWith("<#") && hexCode.endsWith(">")) {
                colorCode = hexCode.substring(2, 8);
            } else if (hexCode.startsWith("&#")) {
                colorCode = hexCode.substring(2, 8);
            }
            String minecraftColorCode = convertHexToColorCode(colorCode);
            matcher.appendReplacement(buffer, minecraftColorCode);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String convertHexToColorCode(String hexCode) {
        char[] ch = hexCode.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c : ch) {
            builder.append("&").append(c);
        }
        return builder.toString();
    }

    private boolean containsHexColor(String message) {
        String hexColorPattern = "(?i)&#[a-f0-9]{6}";
        return message.matches(".*" + hexColorPattern + ".*");
    }

    private String applyPlaceholder(OfflinePlayer player, String text) {
        if (!instance.isPAPI()) {
            return text;
        }
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
