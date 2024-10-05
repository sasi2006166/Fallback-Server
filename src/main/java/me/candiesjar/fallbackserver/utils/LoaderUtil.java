package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.cache.OnlineLobbiesManager;
import me.candiesjar.fallbackserver.cache.ServerTypeManager;
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
            Utils.printDebug("§7[§c!§7] Please check the 'settings.fallback' section", true);
            return;
        }

        for (String key : section.getKeys(false)) {
            if (key.equalsIgnoreCase("placeholder")) {
                continue;
            }

            if (key.equalsIgnoreCase("default")) {
                List<String> servers = section.getStringList(key + ".servers");

                if (servers.isEmpty()) {
                    Utils.printDebug("§7[§c!§7] There are no default lobbies", true);
                    Utils.printDebug("§7[§c!§7] Please add your lobbies to the 'default' section", true);
                    break;
                }

                ServerType serverType = new ServerType(key, null, servers, false);
                serverTypeManager.put(key, serverType);
                onlineLobbiesManager.firstLoad(key);
                continue;
            }

            List<String> servers = section.getStringList(key + ".servers");

            if (servers.isEmpty()) {
                Utils.printDebug("§7[§c!§7] There are no servers", true);
                Utils.printDebug("§7[§c!§7] Please add your servers to the '" + key + "' section", true);
                break;
            }

            List<String> lobbies = section.getStringList(key + ".lobbies");
            String mode = section.getString(key + ".mode");
            boolean reconnect = false;

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

            ServerType serverType = new ServerType(key, servers, lobbies, reconnect);
            serverTypeManager.put(key, serverType);
            onlineLobbiesManager.firstLoad(key);
        }
    }
}