package me.candiesjar.fallbackserver.utils;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;

@UtilityClass
public class ServerUtils {

    public boolean isMaintenance(RegisteredServer registeredServer) {

        boolean useMaintenance = FallbackServerVelocity.getInstance().isUseMaintenance();

        if (useMaintenance) {
            MaintenanceProxy api = (MaintenanceProxy) MaintenanceProvider.get();
            Server server = api.getServer(registeredServer.getServerInfo().getName());

            return api.isMaintenance(server);

        }
        return false;
    }

}
