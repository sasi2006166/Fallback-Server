package me.candiesjar.fallbackserver.utils.system;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.config.BungeeConfig;
import me.candiesjar.fallbackserver.utils.Utils;
import net.md_5.bungee.api.ProxyServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@UtilityClass
public class UpdateUtil {

    @Getter
    private String remoteVersion = "Loading";
    @Getter
    private boolean updateAvailable = false;

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ProxyServer proxyServer = ProxyServer.getInstance();

    public void checkUpdates() {
        boolean updater = BungeeConfig.UPDATER.getBoolean();

        if (!updater) {
            return;
        }

        proxyServer.getScheduler().runAsync(fallbackServerBungee, () -> {
            try {
                URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Utils.printDebug("Cannot fetch updates. HTTP response code: " + responseCode, true);
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    remoteVersion = reader.readLine();
                }

                updateAvailable = !fallbackServerBungee.getDescription().getVersion().equals(remoteVersion);
            } catch (IOException e) {
                Utils.printDebug("Cannot fetch updates", true);
            }
        });
    }
}
