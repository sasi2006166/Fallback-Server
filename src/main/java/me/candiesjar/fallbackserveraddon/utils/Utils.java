package me.candiesjar.fallbackserveraddon.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Utils {

    public void unregisterEvent(Listener listener) {
        HandlerList.unregisterAll(listener);
    }
}
