package me.candiesjar.fallbackserveraddon.utils;

import com.github.retrooper.packetevents.PacketEventsAPI;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.command.CommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;

@UtilityClass
public class Utils {

    private final FallbackServerAddon plugin = FallbackServerAddon.getInstance();

    public void unregisterEvent(Listener listener) {
        if (listener != null) {
            HandlerList.unregisterAll(listener);
        }
    }

    public void unregisterEvent(PacketEventsAPI api) {
        api.getEventManager().unregisterAllListeners();
        api.terminate();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        plugin.loadConfig();
    }

    @SneakyThrows
    public CommandMap getCommandMap(FallbackServerAddon plugin) {
        Field f = plugin.getServer().getPluginManager().getClass().getDeclaredField("commandMap");
        f.setAccessible(true);
        return (CommandMap) f.get(plugin.getServer().getPluginManager());
    }
}
