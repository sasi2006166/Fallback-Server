package me.candiesjar.fallbackserver.utils.player;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class ChatUtil {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final Pattern pattern = fallbackServerBungee.getPattern();

    public String getString(BungeeMessages bungeeMessages) {
        return fallbackServerBungee.getMessagesConfig().getString(bungeeMessages.getPath());
    }

    public String getString(BungeeMessages bungeeMessages, Placeholder... placeholders) {
        return applyPlaceholders(getString(bungeeMessages), placeholders);
    }

    public String getFormattedString(BungeeMessages bungeeMessages) {
        return formatColor(getString(bungeeMessages));
    }

    public String getFormattedString(BungeeMessages bungeeMessages, Placeholder... placeholders) {
        return formatColor(getString(bungeeMessages, placeholders));
    }

    public List<String> getStringList(BungeeMessages bungeeMessages) {
        return fallbackServerBungee.getMessagesConfig().getStringList(bungeeMessages.getPath());
    }

    public List<String> getStringList(BungeeMessages bungeeMessages, Placeholder... placeholders) {
        return getStringList(bungeeMessages).stream()
                .map(s -> applyPlaceholders(s, placeholders))
                .collect(Collectors.toList());
    }

    public String applyPlaceholders(String s, Placeholder... placeholders) {
        for (Placeholder placeholder : placeholders) {
            s = s.replace(placeholder.getKey(), placeholder.getValue());
        }
        return s;
    }

    public String formatColor(String s) {
        return formatHexColor(s);
    }

    public List<String> formatColor(List<String> list) {
        return list.stream()
                .map(ChatUtil::formatColor)
                .collect(Collectors.toList());
    }

    public TextComponent asComponent(String s) {
        return new TextComponent(s);
    }

    public void sendList(CommandSender commandSender, List<String> stringList) {
        stringList.forEach(message -> commandSender.sendMessage(asComponent(message)));
    }

    public void sendFormattedList(BungeeMessages bungeeMessages, CommandSender commandSender, Placeholder... placeholders) {
        sendList(commandSender, formatColor(getStringList(bungeeMessages, placeholders)));
    }

    public void clearChat(ProxiedPlayer player) {
        for (int i = 0; i < 100; i++) {
            player.sendMessage(new TextComponent(""));
        }
    }

    public String formatHexColor(String message) {
        Matcher matcher = pattern.matcher(message);
        String translated = ChatColor.translateAlternateColorCodes('&', message);

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            StringBuilder colorCode = new StringBuilder("§x");

            for (char ch : hexColor.toCharArray()) {
                colorCode.append("§").append(ch);
            }

            translated = translated.replace("&#" + hexColor, colorCode.toString());
        }

        return translated;
    }

    public boolean checkMessage(String message, String name) {
        List<String> blockedCommands = fallbackServerBungee.getConfig()
                .getStringList("settings.command_blocker_list." + name)
                .stream()
                .map(cmd -> "/" + cmd)
                .collect(Collectors.toList());

        return blockedCommands.stream()
                .anyMatch(blockedCmd -> blockedCmd.equalsIgnoreCase(message));
    }
}
