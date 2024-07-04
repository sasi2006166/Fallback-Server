package me.candiesjar.fallbackserveraddon.listeners.addon;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class PingListener implements Listener {

    private final FallbackServerAddon plugin;
    private final TaskScheduler scheduler;

    private boolean received = false;
    private boolean finished = false;

    public PingListener(FallbackServerAddon plugin) {
        this.plugin = plugin;
        scheduler = UniversalScheduler.getScheduler(plugin);
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

        if (plugin.getConfig().getBoolean("settings.addon.auto_remove", false)) {
            scheduler.runTaskLater(() -> plugin.getServer().getPluginManager().disablePlugin(plugin), plugin.getConfig().getInt("settings.disable_after", 30) * 20L);
            return;
        }

        scheduler.runTaskLater(() -> {
            finished = true;
            Utils.unregisterEvent(this);
        }, plugin.getConfig().getInt("settings.addon.disable_after", 30) * 20L);
    }
}
