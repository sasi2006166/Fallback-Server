package me.candiesjar.fallbackserver.utils;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServerUtils {

    public boolean isMaintenance(RegisteredServer registeredServer) {
        MaintenanceProxy api = (MaintenanceProxy) MaintenanceProvider.get();

        if (api == null) {
            Utils.printDebug("Error in maintenance API, please check if is correctly enabled.", true);
            return false;
        }

        Server server = api.getServer(registeredServer.getServerInfo().getName());

        return api.isMaintenance(server);
    }

}
