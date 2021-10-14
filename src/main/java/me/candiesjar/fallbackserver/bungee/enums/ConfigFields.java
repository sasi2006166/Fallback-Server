package me.candiesjar.fallbackserver.bungee.enums;

import me.candiesjar.fallbackserver.bungee.*;
import net.md_5.bungee.api.*;
import java.util.*;

public enum ConfigFields
{
    PERMISSION("Fallback_Server.command_permission"),
    TAB_COMPLETE("Fallback_Server.command_tab_complete"),
    COMMAND_WITHOUT_PERMISSION("Fallback_Server.command_without_permission"),
    STATS("Fallback_Server.use_stats"),
    UPDATE_CHECKER("Fallback_Server.check_updates"),
    MAX_PLAYERS("Fallback_Server.max_players"),
    SPREAD_MODE("Fallback_Server.spread_mode"),

    USE_HUB_COMMAND("Hub.commands.enable_command"),
    HUB_COMMANDS("Hub.commands.command_aliases"),
    LOBBY_SERVER("groups.lobby.servers"),
    DISABLE_SERVERS("Hub.enable_disabled_servers");

    private final String path;

    ConfigFields(String path) {
        this.path = path;
    }

    public boolean getBoolean() {
        return FallbackServerBungee.getInstance().getConfig().getBoolean(path);
    }

    public String getString() {
        return FallbackServerBungee.getInstance().getConfig().getString(path);
    }

    public int getInt() {
        return FallbackServerBungee.getInstance().getConfig().getInt(path);
    }

    public List<String> getStringList() {
        return FallbackServerBungee.getInstance().getConfig().getStringList(path);
    }

    public static String getFormattedString(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
