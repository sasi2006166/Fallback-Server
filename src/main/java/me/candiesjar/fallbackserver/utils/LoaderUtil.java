package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
import me.candiesjar.fallbackserver.objects.ServerType;
import net.md_5.bungee.config.Configuration;

import java.util.List;

@UtilityClass
public class LoaderUtil {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final ServerTypeManager serverTypeManager = fallbackServerBungee.getServerTypeManager();
    private final OnlineLobbiesManager onlineLobbiesManager = fallbackServerBungee.getOnlineLobbiesManager();

    public void loadServers(Configuration section) {

        boolean exists = fallbackServerBungee.getConfig().getSection("settings.fallback") != null;
        fallbackServerBungee.getLogger().info("Exists: " + exists);

        if (section == null) {
            ErrorHandler.add(Severity.ERROR, "Fallback section is either missing or empty");
            Utils.printDebug("§7[§c!§7] There is an error in your configuration", true);
            Utils.printDebug("§7[§c!§7] Please check the 'settings.fallback' section", true);
            return;
        }

        for (String key : section.getKeys()) {
            if (key.equalsIgnoreCase("placeholder")) {
                continue;
            }

            if (!section.contains("default")) {
                ErrorHandler.add(Severity.ERROR, "Lobbies section is missing");
                Utils.printDebug("§7[§c!§7] There is an error in your configuration", true);
                Utils.printDebug("§7[§c!§7] Please check the 'settings.fallback' section", true);
                return;
            }

            if (key.equalsIgnoreCase("default")) {
                List<String> servers = section.getStringList(key + ".servers");

                if (servers.isEmpty()) {
                    ErrorHandler.add(Severity.ERROR, "Lobbies are missing");
                    Utils.printDebug("§7[§c!§7] There are no default lobbies", true);
                    Utils.printDebug("§7[§c!§7] Please add some servers to the 'default' section", true);
                    break;
                }

                ServerType serverType = new ServerType(key, null, servers, false);
                serverTypeManager.put(key, serverType);
                onlineLobbiesManager.firstLoad(key);
                continue;
            }

            List<String> servers = section.getStringList(key + ".servers");

            if (servers.isEmpty()) {
                ErrorHandler.add(Severity.ERROR, "Group " + key + " is missing servers");
                Utils.printDebug("§7[§c!§7] There are no servers", true);
                Utils.printDebug("§7[§c!§7] Please add some servers to the '" + key + "' section", true);
                continue;
            }

            List<String> lobbies = section.getStringList(key + ".lobbies");

            if (lobbies.isEmpty()) {
                ErrorHandler.add(Severity.ERROR, "Group " + key + " is missing lobbies");
                continue;
            }

            String mode = section.getString(key + ".mode");
            boolean reconnect = false;

            switch (mode) {
                case "RECONNECT":
                    reconnect = true;
                    break;
                case "DEFAULT":
                case "FALLBACK":
                default:
                    break;
            }

            ServerType serverType = new ServerType(key, servers, lobbies, reconnect);
            serverTypeManager.put(key, serverType);
            onlineLobbiesManager.firstLoad(key);
        }
    }
}
