package me.candiesjar.fallbackserver.managers;

import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;

import java.util.List;

@UtilityClass
public class ServerManager {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();
    private final Configuration list = fallbackServerBungee.getConfig().getSection("settings.fallback");

    public String getGroupByServer(String name) {
        for (String s : list.getKeys()) {
            List<String> servers = list.getStringList(s + ".servers");
            if (!servers.contains(name)) {
                continue;
            }
            return s;
        }

        return null;
    }

    public String getGroupByName(String name) {
        for (String s : list.getKeys()) {
            if (!s.equalsIgnoreCase(name)) {
                continue;
            }
            return s;
        }

        return null;
    }

    public boolean checkMaintenance(ServerInfo serverInfo) {
        if (!fallbackServerBungee.isMaintenance()) {
            return false;
        }

        MaintenanceProxy api = (MaintenanceProxy) MaintenanceProvider.get();

        Server server = api.getServer(serverInfo.getName());

        return api.isMaintenance(server);
    }
}
