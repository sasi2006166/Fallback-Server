package me.candiesjar.fallbackserver.utils.chat;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeMessages;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ChatUtil {
    private static final FallbackServerBungee instance = FallbackServerBungee.getInstance();
    private static final TextComponent usableComponent = new TextComponent();

    public String getString(BungeeMessages bungeeMessages) {
        return instance.getMessagesConfig().getString(bungeeMessages.getPath());
    }

    public String getString(BungeeMessages bungeeMessages, PlaceHolder... placeHolders) {
        return applyPlaceHolder(getString(bungeeMessages), placeHolders);
    }

    public String getFormattedString(BungeeMessages bungeeMessages) {
        return color(getString(bungeeMessages));
    }

    public String getFormattedString(BungeeMessages bungeeMessages, PlaceHolder... placeHolders) {
        return color(getString(bungeeMessages, placeHolders));
    }

    public List<String> getStringList(BungeeMessages bungeeMessages) {
        return instance.getMessagesConfig().getStringList(bungeeMessages.getPath());
    }

    public List<String> getStringList(BungeeMessages bungeeMessages, PlaceHolder... placeHolders) {
        List<String> newList = new ArrayList<>();

        for (String s : getStringList(bungeeMessages)) {
            s = applyPlaceHolder(s, placeHolders);
            newList.add(s);
        }

        return newList;
    }

    public String applyPlaceHolder(String s, PlaceHolder... placeHolders) {
        for (PlaceHolder placeHolder : placeHolders) {
            s = s.replace(placeHolder.getKey(), placeHolder.getValue());
        }

        return s;
    }

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
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

    public void sendFormattedList(BungeeMessages bungeeMessages, CommandSender commandSender, PlaceHolder... placeHolders) {
        sendList(commandSender, color(getStringList(bungeeMessages, placeHolders)));
    }
}
