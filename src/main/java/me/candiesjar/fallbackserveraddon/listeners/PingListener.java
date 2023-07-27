package me.candiesjar.fallbackserveraddon.listeners;

import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class PingListener implements Listener {

    private final FallbackServerAddon plugin;
    private boolean done = false;

    public PingListener(FallbackServerAddon plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPing(ServerListPingEvent event) {

        if (!plugin.isLocked()) {
            return;
        }

        event.setMaxPlayers(-1);

        if (done) {
            return;
        }

        done = true;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getServer().getPluginManager().disablePlugin(plugin), 30L * 20L);
    }
}
