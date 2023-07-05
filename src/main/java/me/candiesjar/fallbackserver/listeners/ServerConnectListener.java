package me.candiesjar.fallbackserver.listeners;

import me.candiesjar.fallbackserver.FallbackServerBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.QueueManager;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.QueueServer;

public class ServerConnectListener implements Listener {

    private final FallbackServerBungee plugin;

    public ServerConnectListener(FallbackServerBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent event) {

        ProxiedPlayer player = event.getPlayer();
        ServerInfo target = event.getTarget();

        boolean useAjQueue = plugin.isAjQueue();

        if (useAjQueue) {
            AdaptedPlayer adaptedPlayer = AjQueueAPI.getInstance().getPlatformMethods().getPlayer(player.getName());
            QueueManager queueManager = AjQueueAPI.getInstance().getQueueManager();
            QueueServer queueServer = queueManager.findServer(target.getName());

            if (!queueServer.canAccess(adaptedPlayer)) {
                System.out.println("Server in queue");
                event.setCancelled(true);
                return;
            }
        }

    }

}


