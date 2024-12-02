package me.candiesjar.fallbackserver.utils;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class Utils {

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();
    @Getter
    private static String remoteVersion = "Loading";

    @SneakyThrows(Exception.class)
    public CompletableFuture<Boolean> getUpdates() {

        if (fallbackServerVelocity.isBeta()) {
            return CompletableFuture.supplyAsync(() -> false);
        }

        return CompletableFuture.supplyAsync(() -> {
            boolean isUpdateAvailable;
            URLConnection connection;
            try {
                connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=86398").openConnection();
                try (InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream())) {
                    try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                        remoteVersion = bufferedReader.readLine();
                        isUpdateAvailable = !fallbackServerVelocity.getVersion().equalsIgnoreCase(remoteVersion);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return isUpdateAvailable;
        });
    }

    public void printDebug(String s) {
        fallbackServerVelocity.getComponentLogger().info("[INFO] {}", s);
    }

    public void printDebug(String s, boolean exception) {
        if (exception) {
            fallbackServerVelocity.getComponentLogger().error("[ERROR] {}", s);
            return;
        }

        fallbackServerVelocity.getComponentLogger().warn("[DEBUG] {}", s);
    }

    public String getDots(int s) {
        return switch (s % 4) {
            case 1 -> ".";
            case 2 -> "..";
            case 3 -> "...";
            default -> "";
        };
    }

    public void saveServers(List<String> servers) {
        fallbackServerVelocity.getServersTextFile().getConfig().set("servers", servers);
        fallbackServerVelocity.getServersTextFile().save();
        fallbackServerVelocity.getServersTextFile().reload();
    }

}
