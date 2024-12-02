package me.candiesjar.fallbackserver.managers;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.utils.Utils;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.List;

@UtilityClass
public class ServerManager {

    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();
    private final ConfigurationSection list = fallbackServerVelocity.getConfigTextFile().getConfig().getConfigurationSection("settings.fallback");

    public String getGroupByServer(String name) {
        for (String s : list.getKeys(false)) {
            List<String> servers = list.getStringList(s + ".servers");
            if (!servers.contains(name)) {
                continue;
            }
            return s;
        }

        return null;
    }

    public String getGroupByName(String name) {
        for (String s : list.getKeys(false)) {
            if (!s.equalsIgnoreCase(name)) {
                continue;
            }
            return s;
        }

        return null;
    }

    public boolean checkIfGroupExists(String group) {
        ConfigurationSection section = fallbackServerVelocity.getConfigTextFile().getConfig().getConfigurationSection("settings.fallback");
        ConfigurationSection servers = fallbackServerVelocity.getServersTextFile().getConfig().getConfigurationSection("servers");

        return section.getKeys(false).contains(group) || servers.getKeys(false).contains(group);
    }

    public boolean checkMaintenance(RegisteredServer registeredServer) {
        if (!fallbackServerVelocity.isMaintenance()) {
            return false;
        }

        MaintenanceProxy api = (MaintenanceProxy) MaintenanceProvider.get();

        if (api == null) {
            Utils.printDebug("Error in maintenance API, please check if is correctly enabled.", true);
            return false;
        }

        Server server = api.getServer(registeredServer.getServerInfo().getName());

        return api.isMaintenance(server);
    }
}
