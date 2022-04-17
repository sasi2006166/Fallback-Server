package me.candiesjar.fallbackserver.velocity.utils;

import com.velocitypowered.api.proxy.Player;
import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;
import me.candiesjar.fallbackserver.velocity.FallbackServerVelocity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class VelocityUtils {

    private static FallbackServerVelocity plugin;
    private static String remoteVersion = "Loading";
    private static boolean updateAvailable = false;

    public VelocityUtils(FallbackServerVelocity plugin) {
        VelocityUtils.plugin = plugin;
    }

    public static void getUpdates() {
        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            try {

                final URLConnection connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398").openConnection();

                remoteVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                updateAvailable = !FallbackServerBungee.getInstance().getDescription().getVersion().equals(remoteVersion);

            } catch (IOException ignored) {
                FallbackServerBungee.getInstance().getLogger().severe("Cannot fetch updates, check your firewall settings.");
            }
        });

    }

    public static String getRemoteVersion() {
        return remoteVersion;
    }

    public static boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public void sendFallbackTitle(Player player) {

    }


}
