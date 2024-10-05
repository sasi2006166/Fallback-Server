package me.candiesjar.fallbackserver.utils.player;

import com.google.common.collect.Lists;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.text.Placeholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class ChatUtil {
    private final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    public String getString(VelocityMessages velocityMessages) {
        return instance.getMessagesTextFile().getConfig().getString(velocityMessages.getPath());
    }

    public String getString(VelocityMessages velocityMessages, Placeholder... placeholders) {
        return applyPlaceHolder(getString(velocityMessages), placeholders);
    }

    public String getFormattedString(VelocityMessages velocityMessages, Placeholder... placeholders) {
        return color(getString(velocityMessages, placeholders));
    }

    public List<String> getStringList(VelocityMessages velocityMessages) {
        return instance.getMessagesTextFile().getConfig().getStringList(velocityMessages.getPath());
    }

    public List<String> getStringList(VelocityMessages velocityMessages, Placeholder... placeholders) {
        List<String> newList = new ArrayList<>();

        for (String s : getStringList(velocityMessages)) {
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
        String hex = convertHexColors(s);
        return hex.replace("&", "§");
    }

    public List<String> color(List<String> list) {
        return list.stream().map(ChatUtil::color).collect(Collectors.toList());
    }

    public void sendList(CommandSource commandSource, List<String> stringList) {
        for (String message : stringList) {
            commandSource.sendMessage(Component.text(message));
        }
    }

    public void sendFormattedList(VelocityMessages velocityMessages, CommandSource commandSource, Placeholder... placeholders) {
        sendList(commandSource, color(getStringList(velocityMessages, placeholders)));
    }

    public String componentToString(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public String convertHexColors(String message) {
        if (!containsHexColor(message)) {
            return message;
        }

        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return message;
    }

    public boolean containsHexColor(String message) {
        String hexColorPattern = "(?i)&#[a-f0-9]{6}";
        return message.matches(".*" + hexColorPattern + ".*");
    }

    public boolean checkMessage(String message, List<String> stringList) {
        List<String> list = Lists.newArrayList();

        for (String s : stringList) {
            String toLowerCase = s.toLowerCase();
            list.add(toLowerCase);
        }

        return list.contains(message.toLowerCase());
    }

    public void clearChat(Player player) {
        for (int i = 0; i < 100; i++) {
            player.sendMessage(Component.text(""));
        }
    }

}
