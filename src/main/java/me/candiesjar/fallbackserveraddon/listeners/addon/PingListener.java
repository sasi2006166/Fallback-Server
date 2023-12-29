package me.candiesjar.fallbackserveraddon.listeners.addon;

import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class PingListener implements Listener {

    private final FallbackServerAddon plugin;

    private boolean received = false;
    private boolean finished = false;

    public PingListener(FallbackServerAddon plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPing(ServerListPingEvent event) {

        if (finished) {
            return;
        }

        if (!plugin.isLocked()) {
            return;
        }

        event.setMaxPlayers(plugin.getConfig().getInt("settings.addon.override_player_count_number", -1));

        if (received) {
            return;
        }

        received = true;

        if (plugin.getConfig().getBoolean("settings.addon.auto_remove", true)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getServer().getPluginManager().disablePlugin(plugin), plugin.getConfig().getInt("settings.disable_after", 30) * 20L);
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            finished = true;
            Utils.unregisterEvent(this);
        }, plugin.getConfig().getInt("settings.addon.disable_after", 30) * 20L);
    }
}
