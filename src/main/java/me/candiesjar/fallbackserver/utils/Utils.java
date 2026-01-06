package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;

import java.util.List;

@UtilityClass
public class Utils {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();

    public String getDots(int s) {
        switch (s % 4) {
            case 1:
                return ".";
            case 2:
                return "..";
            case 3:
                return "...";
            default:
                return "";
        }
    }

    public void saveServers(List<String> servers) {
        fallbackServerBungee.getServersTextFile().getConfig().set("servers", servers);
        fallbackServerBungee.getServersTextFile().save();
        fallbackServerBungee.getServersTextFile().reload();
    }

    public void printDebug(String s, boolean exception) {
        if (!exception) {
            fallbackServerBungee.getLogger().warning("[DEBUG] " + s);
        } else {
            fallbackServerBungee.getLogger().severe("[ERROR] " + s);
        }
    }

}
