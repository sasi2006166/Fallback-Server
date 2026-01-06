package me.candiesjar.fallbackserver.utils.player;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.config.BungeeMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ChatUtil {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public String getString(BungeeMessages bungeeMessages) {
        return fallbackServerBungee.getMessagesConfig().getString(bungeeMessages.getPath());
    }

    public String getString(BungeeMessages bungeeMessages, Placeholder... placeholders) {
        return applyPlaceholders(getString(bungeeMessages), placeholders);
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

    public void sendList(CommandSender commandSender, List<String> stringList) {
        Audience audience = fallbackServerBungee.adventure().sender(commandSender);
        stringList.forEach(message -> {
            audience.sendMessage(asComponent(message));
        });
    }

    public void sendFormattedList(BungeeMessages bungeeMessages, CommandSender commandSender, Placeholder... placeholders) {
        sendList(commandSender, formatColor(getStringList(bungeeMessages, placeholders)));
    }

    public String formatColor(String s) {
        return miniMessage.serialize(miniMessage.deserialize(s));
    }

    public List<String> formatColor(List<String> s) {
        return s.stream()
                .map(ChatUtil::formatColor)
                .collect(Collectors.toList());
    }

    public Component asComponent(String s) {
        return miniMessage.deserialize(s);
    }

    public String applyPlaceholders(String s, Placeholder... placeholders) {
        for (Placeholder placeholder : placeholders) {
            s = s.replace(placeholder.getKey(), placeholder.getValue());
        }
        return s;
    }

    public void clearChat(ProxiedPlayer player) {
        Audience audience = fallbackServerBungee.adventure().sender(player);
        for (int i = 0; i < 100; i++) {
            audience.sendMessage(Component.empty());
        }
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
