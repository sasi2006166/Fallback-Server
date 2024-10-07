package me.candiesjar.fallbackserver.utils.player;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class ChatUtil {

    private FallbackServerVelocity getInstance() {
        return FallbackServerVelocity.getInstance();
    }

    private final Pattern pattern = getInstance().getPattern();

    public String getString(VelocityMessages velocityMessages) {
        return getInstance().getMessagesTextFile().getConfig().getString(velocityMessages.getPath());
    }

    public String getString(VelocityMessages velocityMessages, Placeholder... placeholders) {
        return applyPlaceholders(getString(velocityMessages), placeholders);
    }

    public String getFormattedString(VelocityMessages velocityMessages, Placeholder... placeholders) {
        return formatColor(getString(velocityMessages, placeholders));
    }

    public List<String> getStringList(VelocityMessages velocityMessages) {
        return getInstance().getMessagesTextFile().getConfig().getStringList(velocityMessages.getPath());
    }

    public List<String> getStringList(VelocityMessages velocityMessages, Placeholder... placeholders) {
        return getStringList(velocityMessages).stream()
                .map(s -> applyPlaceholders(s, placeholders))
                .collect(Collectors.toList());
    }

    public String applyPlaceholders(String message, Placeholder... placeholders) {
        for (Placeholder placeholder : placeholders) {
            message = message.replace(placeholder.getKey(), placeholder.getValue());
        }
        return message;
    }

    public String formatColor(String message) {
        return formatHexColor(message.replace("&", "§"));
    }

    public List<String> formatColor(List<String> messages) {
        return messages.stream()
                .map(ChatUtil::formatColor)
                .collect(Collectors.toList());
    }

    public void sendList(CommandSource commandSource, List<String> messages) {
        messages.forEach(message -> commandSource.sendMessage(Component.text(message)));
    }

    public void sendFormattedList(VelocityMessages velocityMessages, CommandSource commandSource, Placeholder... placeholders) {
        sendList(commandSource, formatColor(getStringList(velocityMessages, placeholders)));
    }

    public String componentToString(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public String formatHexColor(String message) {
        if (!containsHexColor(message)) {
            return message;
        }

        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String hexCode = matcher.group();
            String formattedHex = hexCode.replace('#', 'x').replaceAll("(.)", "&$1");
            message = message.replace(hexCode, formattedHex);
        }

        return message;
    }

    public boolean containsHexColor(String message) {
        return message.matches(".*#[a-fA-F0-9]{6}.*");
    }

    public boolean checkMessage(String message, List<String> blockedMessages) {
        List<String> lowerCasedBlockedMessages = blockedMessages.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return lowerCasedBlockedMessages.contains(message.toLowerCase());
    }

    public void clearChat(Player player) {
        for (int i = 0; i < 100; i++) {
            player.sendMessage(Component.text(""));
        }
    }
}
