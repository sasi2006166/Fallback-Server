package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
import me.candiesjar.fallbackserver.enums.Severity;
import me.candiesjar.fallbackserver.handler.ErrorHandler;
import me.candiesjar.fallbackserver.objects.ServerType;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.List;

@UtilityClass
public class LoaderUtil {

    private final FallbackServerVelocity fallbackServerBungee = FallbackServerVelocity.getInstance();
    private final ServerTypeManager serverTypeManager = fallbackServerBungee.getServerTypeManager();
    private final OnlineLobbiesManager onlineLobbiesManager = fallbackServerBungee.getOnlineLobbiesManager();

    public void loadServers(ConfigurationSection section) {
        if (section == null) {
            Utils.printDebug("§7[§c!§7] There is an error in your configuration", true);
            Utils.printDebug("§7[§c!§7] Please check the 'settings.fallback' section, plugin will now disable.", true);
            return;
        }

        for (String key : section.getKeys(false)) {
            if (key.equalsIgnoreCase("placeholder")) {
                continue;
            }

            if (key.equalsIgnoreCase("default")) {
                List<String> lobbies = section.getStringList(key + ".servers");

                if (lobbies.isEmpty()) {
                    ErrorHandler.add(Severity.ERROR, "[LOADER] Default lobbies are missing");
                    Utils.printDebug("§7[§c!§7] There are no default lobbies", true);
                    Utils.printDebug("§7[§c!§7] Please add your lobbies to the 'default' section", true);
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

            switch (mode) {
                case "RECONNECT":
                    fallbackServerBungee.loadReconnect();

                    if (!fallbackServerBungee.isReconnect()) {
                        break;
                    }

                    reconnect = true;
                    break;
                case "DEFAULT":
                case "FALLBACK":
                default:
                    break;
            }

            loadServerType(key, servers, lobbies, reconnect);
        }
    }

    private boolean checkEmpty(List<String> list) {
        return list == null || list.isEmpty();
    }

    private void loadServerType(String key, List<String> servers, List<String> lobbies, boolean reconnect) {
        ServerType serverType = new ServerType(key, servers, lobbies, reconnect);
        serverTypeManager.put(key, serverType);
        onlineLobbiesManager.firstLoad(key);
    }

}