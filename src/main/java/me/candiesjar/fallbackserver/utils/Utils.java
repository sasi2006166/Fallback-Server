package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import net.md_5.bungee.api.ProxyServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@UtilityClass
public class Utils {

    private String remoteVersion = "Loading";
    private boolean updateAvailable = false;

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = ProxyServer.getInstance();

    public void checkUpdates() {
        proxyServer.getScheduler().runAsync(fallbackServerBungee, () -> {
            try {
                URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    fallbackServerBungee.getLogger().severe("Cannot fetch updates. HTTP response code: " + responseCode);
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                remoteVersion = reader.readLine();
                reader.close();

                updateAvailable = fallbackServerBungee.getDescription().getVersion().equals(remoteVersion);

                connection.disconnect();
            } catch (IOException e) {
                fallbackServerBungee.getLogger().severe("Cannot fetch updates. Exception: " + e.getMessage());
            }
        });
    }

    public void printDebug(String s, boolean exception) {

        if (!exception) {
            fallbackServerBungee.getLogger().warning("[DEBUG] " + s);
            return;
        }

        fallbackServerBungee.getLogger().severe("[ERROR] " + s);

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
        fallbackServerBungee.getConfig().set(section, BungeeConfig.LOBBIES_LIST.getStringList());
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getRemoteVersion() {
        return remoteVersion;
    }
}
