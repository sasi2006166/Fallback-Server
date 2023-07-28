package me.candiesjar.fallbackserver.utils;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.netty.ChannelWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Getter
@UtilityClass
public class Utils {

    @Getter
    private String remoteVersion = "Loading";
    @Getter
    private boolean updateAvailable = false;

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = ProxyServer.getInstance();
    private Field userChannelWrapperField = null;

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

    public void saveServers(List<String> servers) {
        fallbackServerBungee.getServersTextFile().getConfig().set("servers", servers);
        fallbackServerBungee.getServersTextFile().save();
        fallbackServerBungee.getServersTextFile().reload();
    }

    public ChannelWrapper getUserChannelWrapper(UserConnection user) {
        if (user != null) {
            try {
                return (ChannelWrapper) userChannelWrapperField.get(user);
            } catch (ClassCastException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static {
        for (Field f : UserConnection.class.getDeclaredFields()) {
            if (ChannelWrapper.class.isAssignableFrom(f.getType())) {
                userChannelWrapperField = f;
                userChannelWrapperField.setAccessible(true);
                break;
            }
        }
    }
}
