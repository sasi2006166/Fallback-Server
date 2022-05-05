package me.candiesjar.fallbackserver.utils;

import me.candiesjar.fallbackserver.FallbackServerVelocity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class VelocityUtils {

    private static final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();
    private static String remoteVersion = "Loading";
    private static boolean updateAvailable = false;

    public static void getUpdates() {
        instance.getServer().getScheduler().buildTask(instance, () -> {
            try {

                final URLConnection connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398").openConnection();

                remoteVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                updateAvailable = !instance.getVersion().get().equals(remoteVersion);

            } catch (IOException ignored) {
                FallbackServerVelocity.getInstance().getLogger().error("Cannot fetch updates, check your firewall settings.");
            }
        }).schedule();

    }

    public static boolean checkMessage(String message, String name) {
            for (String text : instance.getConfigTextFile().getConfig().getStringList("settings.disabled_servers_list." + name)) {
            text = "/" + text;
            if (text.equalsIgnoreCase(message)) {
                return true;
            }
        }
        return false;
    }

    public static String getRemoteVersion() {
        return remoteVersion;
    }

    public static boolean isUpdateAvailable() {
        return updateAvailable;
    }

}
