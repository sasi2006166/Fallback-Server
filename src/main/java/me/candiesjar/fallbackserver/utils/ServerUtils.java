package me.candiesjar.fallbackserver.utils;

import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.config.ServerInfo;

@UtilityClass
public class ServerUtils {

    public boolean checkMaintenance(ServerInfo serverInfo) {

        MaintenanceProxy api = (MaintenanceProxy) MaintenanceProvider.get();
        Server server = api.getServer(serverInfo.getName());

        return api.isMaintenance(server);

    }

}
