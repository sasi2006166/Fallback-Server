package me.candiesjar.fallbackserver.utils;

import me.candiesjar.fallbackserver.FallbackServer;
import net.md_5.bungee.api.ChatColor;

import java.util.List;

public enum Fields {

    PERMISSION("Fallback.MainPermission"),
    RELOADMESSAGE("Fallback.ReloadMessage"),
    NOPERMISSION("Fallback.NoPermission"),
    MAINCOMMAND("Fallback.MainCommand"),
    NOTPLAYER("Fallback.NotPlayer"),
    ALREADYINHUB("Fallback.AlreadyConnected"),
    HUBCONNESSION("Fallback.HubMessage"),
    CORRECTSYNTAX("Fallback.CorrectSyntax"),
    CONNECTEDTOFALLBACK("Fallback.ServerCrash"),
    LOBBYSERVER("Fallback.LobbyServer"),

    // Titles
    FADEIN("Title.FadeIn"),
    FADEOUT("Title.FadeOut"),
    TITLESTAY("Title.Stay"),
    TITLE("Title.Title"),
    TITLESUBTITLE("Title.SubTitle"),
    USETITLE("Title.Enabled");

    private final String path;

    Fields(String path) {
        this.path = path;
    }

    public boolean getBoolean() {
        return FallbackServer.instance.getConfigFile().getBoolean(path);
    }
    public String getFormattedString() {
        return ChatColor.translateAlternateColorCodes('&', FallbackServer.instance.getConfigFile().getString(path));
    }

    public String getString() {
        return FallbackServer.instance.getConfigFile().getString(path);
    }

    public int getInt() {
        return FallbackServer.instance.getConfigFile().getInt(path);
    }

    public List<String> getStringList() {
        return FallbackServer.instance.getConfigFile().getStringList(path);
    }

    public static String getFormattedString(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
