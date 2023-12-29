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

    private final FallbackServerAddon plugin = FallbackServerAddon.getInstance();

    public void unregisterEvent(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    public boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        return true;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        plugin.saveDefaultConfig();
    }
}
