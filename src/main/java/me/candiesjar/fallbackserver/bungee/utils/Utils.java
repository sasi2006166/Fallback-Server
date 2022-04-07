package me.candiesjar.fallbackserver.bungee.utils;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import net.md_5.bungee.api.ProxyServer;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Utils {

    private static String remoteVersion = "Loading";
    private static boolean updateAvailable = false;

    public static void checkUpdates() {
        ProxyServer.getInstance().getScheduler().runAsync(FallbackServerBungee.getInstance(), () -> {
            try {

                final URLConnection connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398").openConnection();

                remoteVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                updateAvailable = !FallbackServerBungee.getInstance().getDescription().getVersion().equals(remoteVersion);

            } catch (IOException ignored) {
                FallbackServerBungee.getInstance().getLogger().severe("Cannot fetch updates, check your firewall settings.");
            }

        });
    }

    public static boolean checkMessage(String message, String name) {
        for (String text : FallbackServerBungee.getInstance().getConfig().getStringList("settings.disabled_servers_list." + name)) {
            text = "/" + text;
            if (text.equalsIgnoreCase(message)) {
                return true;
            }
        }
        return false;
    }

    public static void writeToServerList(String section, Object object) {
        FallbackServerBungee.getInstance().getServerList().add(object.toString());
        FallbackServerBungee.getInstance().getConfig().set(section, FallbackServerBungee.getInstance().getServerList());
        // FallbackServerBungee.getInstance().saveConfiguration();
    }

    public static boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public static String getRemoteVersion() {
        return remoteVersion;
    }
}
