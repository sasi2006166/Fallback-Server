package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import net.md_5.bungee.api.ProxyServer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@UtilityClass
public class Utils {

    private String remoteVersion = "Loading";
    private boolean updateAvailable = false;

    private static final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    public void checkUpdates() {
        ProxyServer.getInstance().getScheduler().runAsync(FallbackServerBungee.getInstance(), () -> {
            try {

                final URLConnection connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398").openConnection();

                remoteVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                updateAvailable = !instance.getDescription().getVersion().equals(remoteVersion);

            } catch (IOException ignored) {
                instance.getLogger().severe("Cannot fetch updates, check your firewall settings.");
            }

        });
    }

    public boolean checkMessage(String message, String name) {
        for (String text : instance.getConfig().getStringList("settings.command_blocker_list." + name)) {
            text = "/" + text;
            if (text.equalsIgnoreCase(message)) {
                return true;
            }
        }
        return false;
    }

    public String getDots(int s) {
        switch (s % 4) {
            case 0:
            default:
                return "";
            case 1:
                return ".";
            case 2:
                return "..";
            case 3:
                return "...";
        }
    }

    public void writeToServerList(String section, String arguments) {
        BungeeConfig.LOBBIES_LIST.getStringList().add(arguments);
        instance.getConfig().set(section, BungeeConfig.LOBBIES_LIST.getStringList());
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getRemoteVersion() {
        return remoteVersion;
    }
}
