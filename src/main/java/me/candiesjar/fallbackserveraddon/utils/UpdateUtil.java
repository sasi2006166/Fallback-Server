package me.candiesjar.fallbackserveraddon.utils;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class UpdateUtil {

    private final FallbackServerAddon plugin = FallbackServerAddon.getInstance();

    @Getter
    private String remoteVersion = "Loading";

    @Getter
    private boolean updateAvailable = false;

    @SneakyThrows
    public void checkForUpdates() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    plugin.getLogger().severe("Cannot fetch updates. HTTP response code: " + responseCode);
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    remoteVersion = reader.readLine();
                }

                updateAvailable = !plugin.getDescription().getVersion().equals(remoteVersion);
            } catch (IOException e) {
                plugin.getLogger().severe("Cannot fetch for updates.");
            }
        });
    }

    public void sendUpdateMessage() {
        if (updateAvailable) {
            plugin.getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§e!§7] §7An update is available for FallbackServerAddon.");
        }
    }
}
