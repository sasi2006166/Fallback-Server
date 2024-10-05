package me.candiesjar.fallbackserver.utils.player;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class ChatUtil {
    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final TextComponent usableComponent = new TextComponent();

    public String getString(BungeeMessages bungeeMessages) {
        return fallbackServerBungee.getMessagesConfig().getString(bungeeMessages.getPath());
    }

    public String getString(BungeeMessages bungeeMessages, Placeholder... placeholders) {
        return applyPlaceHolder(getString(bungeeMessages), placeholders);
    }

    public String getFormattedString(BungeeMessages bungeeMessages) {
        return color(getString(bungeeMessages));
    }

    public String getFormattedString(BungeeMessages bungeeMessages, Placeholder... placeholders) {
        return color(getString(bungeeMessages, placeholders));
    }

    public List<String> getStringList(BungeeMessages bungeeMessages) {
        return fallbackServerBungee.getMessagesConfig().getStringList(bungeeMessages.getPath());
    }

    public List<String> getStringList(BungeeMessages bungeeMessages, Placeholder... placeholders) {
        List<String> newList = new ArrayList<>();

        for (String s : getStringList(bungeeMessages)) {
            s = applyPlaceHolder(s, placeholders);
            newList.add(s);
        }

        return newList;
    }

    public String applyPlaceHolder(String s, Placeholder... placeholders) {
        for (Placeholder placeHolder : placeholders) {
            s = s.replace(placeHolder.getKey(), placeHolder.getValue());
        }
        return s;
    }

    public String color(String s) {
        return colorHex(s);
    }

    public List<String> color(List<String> list) {
        return list.stream().map(ChatUtil::color).collect(Collectors.toList());
    }

    public TextComponent asComponent(String s) {
        usableComponent.setText(s);
        return usableComponent;
    }

    public void sendList(CommandSender commandSender, List<String> stringList) {
        for (String message : stringList) {
            commandSender.sendMessage(asComponent(message));
        }
    }

    public void sendFormattedList(BungeeMessages bungeeMessages, CommandSender commandSender, Placeholder... placeholders) {
        sendList(commandSender, color(getStringList(bungeeMessages, placeholders)));
    }

    public void clearChat(ProxiedPlayer player) {
        for (int i = 0; i < 100; i++) {
            player.sendMessage(new TextComponent(""));
        }
    }

    public String colorHex(String s) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        for (Matcher matcher = pattern.matcher(s); matcher.find(); matcher = pattern.matcher(s)) {
            String color = s.substring(matcher.start(), matcher.end());
            s = s.replace(color, ChatColor.of(color) + "");
        }
        s = ChatColor.translateAlternateColorCodes('&', s);
        return s;
    }

    public boolean checkMessage(String message, String name) {
        for (String text : fallbackServerBungee.getConfig().getStringList("settings.command_blocker_list." + name)) {
            text = "/" + text;
            if (text.equalsIgnoreCase(message)) {
                return true;
            }
        }
        return false;
    }

}
