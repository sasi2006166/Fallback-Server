package me.candiesjar.fallbackserver.utils;

import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handlers.ErrorHandler;
import me.candiesjar.fallbackserver.objects.ServerType;
import net.md_5.bungee.config.Configuration;

import java.util.List;

public class FallbackGroupsLoader {

    private final ServerTypeManager serverTypeManager;
    private final OnlineLobbiesManager onlineLobbiesManager;

    public FallbackGroupsLoader(ServerTypeManager serverTypeManager, OnlineLobbiesManager onlineLobbiesManager) {
        this.serverTypeManager = serverTypeManager;
        this.onlineLobbiesManager = onlineLobbiesManager;
    }

    @SuppressWarnings("t")
    public void loadServers(Configuration section, boolean bypass) {
        if (section == null) {
            ErrorHandler.add(Severity.ERROR, "[LOADER] Fallback section is either missing or empty");
            Utils.printDebug("[LOADER] There is an error in your configuration", true);
            Utils.printDebug("[LOADER] Please check the 'settings.fallback' section", true);
            return;
        }

        if (!section.contains("default") && bypass) {
            ErrorHandler.add(Severity.ERROR, "[LOADER] Lobbies section is missing");
            Utils.printDebug("[LOADER] There is an error in your configuration", true);
            Utils.printDebug("[LOADER] Please check the 'settings.fallback.default' section", true);
            return;
        }

        for (String key : section.getKeys()) {
            if (key.equalsIgnoreCase("placeholder")) {
                continue;
            }

            if (key.equalsIgnoreCase("default")) {
                List<String> lobbies = section.getStringList(key + ".servers");

                if (checkEmpty(lobbies)) {
                    ErrorHandler.add(Severity.ERROR, "[LOADER] Default lobbies are missing");
                    Utils.printDebug("[LOADER] There are no default lobbies", true);
                    Utils.printDebug("[LOADER] Please add some servers to the 'default' section", true);
                    break;
                }

                loadServerType(key, null, lobbies, false);
                continue;
            }

            List<String> servers = section.getStringList(key + ".servers");

            if (checkEmpty(servers)) {
                ErrorHandler.add(Severity.ERROR, "[LOADER] Group " + key + " is missing servers");
                Utils.printDebug("[LOADER] There are no server inside '" + key + "' section", true);
                continue;
            }

            List<String> lobbies = section.getStringList(key + ".lobbies");

            if (checkEmpty(lobbies)) {
                ErrorHandler.add(Severity.ERROR, "[LOADER] Group " + key + " is missing lobbies");
                continue;
            }

            String mode = section.getString(key + ".mode", "DEFAULT");
            boolean reconnect = "RECONNECT".equalsIgnoreCase(mode);

            loadServerType(key, servers, lobbies, reconnect);
        }
    }

    private void loadServerType(String key, List<String> servers, List<String> lobbies, boolean reconnect) {
        ServerType serverType = new ServerType(key, servers, lobbies, reconnect);
        serverTypeManager.put(key, serverType);
        onlineLobbiesManager.firstLoad(key);
    }

    private boolean checkEmpty(List<String> list) {
        return list == null || list.isEmpty();
    }
}

