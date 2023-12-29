package me.candiesjar.fallbackserveraddon.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

@UtilityClass
public class Utils {

    public void unregisterEvent(Listener listener) {
        HandlerList.unregisterAll(listener);
    }
}
