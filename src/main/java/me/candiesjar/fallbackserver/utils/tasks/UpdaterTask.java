package me.candiesjar.fallbackserver.utils.tasks;

import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerBungee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdaterTask implements Runnable {

    @Getter
    private static String remoteVersion = "Loading..";

    @Getter
    private static boolean updateAvailable = false;

    private final FallbackServerBungee instance = FallbackServerBungee.getInstance();

    @Override
    public void run() {

        try {

            final URLConnection connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398").openConnection();

            remoteVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            updateAvailable = !instance.getDescription().getVersion().equals(remoteVersion);

        } catch (IOException ignored) {
            instance.getLogger().severe("Cannot fetch updates, check your firewall settings.");
        }

    }


}
