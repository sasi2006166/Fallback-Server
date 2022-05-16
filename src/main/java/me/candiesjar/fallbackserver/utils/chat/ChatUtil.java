package me.candiesjar.fallbackserver.utils.chat;

import com.velocitypowered.api.command.CommandSource;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityMessages;
import me.candiesjar.fallbackserver.objects.PlaceHolder;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ChatUtil {
    private static final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    public String getString(VelocityMessages velocityMessages) {
        return instance.getMessagesTextFile().getConfig().getString(velocityMessages.getPath());
    }

    public String getString(VelocityMessages velocityMessages, PlaceHolder... placeHolders) {
        return applyPlaceHolder(getString(velocityMessages), placeHolders);
    }

    public String getFormattedString(VelocityMessages velocityMessages, PlaceHolder... placeHolders) {
        return color(getString(velocityMessages, placeHolders));
    }

    public List<String> getStringList(VelocityMessages velocityMessages) {
        return instance.getMessagesTextFile().getConfig().getStringList(velocityMessages.getPath());
    }

    public List<String> getStringList(VelocityMessages velocityMessages, PlaceHolder... placeHolders) {
        List<String> newList = new ArrayList<>();

        for (String s : getStringList(velocityMessages)) {
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
        return s.replace("&", "§");
    }

    public List<String> color(List<String> list) {
        return list.stream().map(ChatUtil::color).collect(Collectors.toList());
    }

    public void sendList(CommandSource commandSource, List<String> stringList) {
        for (String message : stringList) {
            commandSource.sendMessage(Component.text(message));
        }
    }

    public void sendFormattedList(VelocityMessages velocityMessages, CommandSource commandSource, PlaceHolder... placeHolders) {
        sendList(commandSource, color(getStringList(velocityMessages, placeHolders)));
    }
}
