package me.candiesjar.fallbackserver.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class VelocityUtils {
    private static String remoteVersion = "Loading";
    private final FallbackServerVelocity instance = FallbackServerVelocity.getInstance();

    @SneakyThrows(Exception.class)
    public CompletableFuture<Boolean> getUpdates() {

        if (instance.isAlpha()) {

            return CompletableFuture.supplyAsync(() -> {
                instance.getLogger().info("§7Updater is disabled in alpha version(s).");
                instance.getLogger().info(" ");
                return false;
            });
        }

        return CompletableFuture.supplyAsync(() -> {
            boolean isUpdateAvailable;
            URLConnection connection;
            try {
                connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398").openConnection();
                try (InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream())) {
                    try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                        remoteVersion = bufferedReader.readLine();
                        isUpdateAvailable = !FallbackServerVelocity.VERSION.equalsIgnoreCase(remoteVersion);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return isUpdateAvailable;
        });
    }

    public boolean checkMessage(String message, List<String> stringList) {
        List<String> list = new ArrayList<>();

        for (String s : stringList) {
            String toLowerCase = s.toLowerCase();
            list.add(toLowerCase);
        }

        return list.contains(message.toLowerCase());
    }

    public String getRemoteVersion() {
        return remoteVersion;
    }
}
