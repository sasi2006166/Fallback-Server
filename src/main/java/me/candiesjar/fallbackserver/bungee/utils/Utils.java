package me.candiesjar.fallbackserver.bungee.utils;

import me.candiesjar.fallbackserver.bungee.FallbackServerBungee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Utils {

    public boolean getUpdates() {
        try {
            URLConnection connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398").openConnection();
            String response = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            return !FallbackServerBungee.getInstance().getDescription().getVersion().equals(response);
        } catch (IOException e) {
            FallbackServerBungee.getInstance().getLogger().severe("Cannot check for updates, maybe something is blocking it?");
        }
        return false;
    }

    public boolean checkMessage(String message, String name) {
        for (String text : FallbackServerBungee.getInstance().getConfig().getStringList("Hub.disabled_servers." + name)) {
            text = "/" + text;
            if (text.equalsIgnoreCase(message)) {
                return true;
            }
        }
        return false;
    }
}
