package me.candiesjar.fallbackserver.listeners;

import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.QueueManager;
import us.ajg0702.queue.api.players.AdaptedPlayer;

public class ServerConnectListener implements Listener {

    private final FallbackServerBungee plugin;

    public ServerConnectListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerSwitch(ServerConnectEvent event) {

        ProxiedPlayer player = event.getPlayer();
        ServerInfo target = event.getTarget();

        boolean useAjQueue = plugin.isUseAjQueue();

        if (useAjQueue) {
            AdaptedPlayer adaptedPlayer = AjQueueAPI.getInstance().getPlatformMethods().getPlayer(player.getName());
            QueueManager queueManager = AjQueueAPI.getInstance().getQueueManager();

            



        }

        boolean useMaintenance = plugin.isUseMaintenance();

        if (useMaintenance) {
            MaintenanceProxy api = (MaintenanceProxy) MaintenanceProvider.get();
            Server server = api.getServer(target.getName());

            boolean isMaintenance = api.isMaintenance(server);

            if (isMaintenance) {
                event.setCancelled(true);

            }
        }

    }

}
