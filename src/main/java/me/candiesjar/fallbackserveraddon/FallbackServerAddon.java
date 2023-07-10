package me.candiesjar.fallbackserveraddon;

import lombok.Getter;
import lombok.Setter;
import me.candiesjar.fallbackserveraddon.listeners.PingListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class FallbackServerAddon extends JavaPlugin {

    @Setter
    private boolean allPluginsLoaded = true;

    @Getter
    private boolean isLocked = false;

    @Getter
    private int max = 0;

    private BukkitTask task;

    @Override
    public void onEnable() {

        getLogger().info("\n" +
                "  ______ _____            _     _             \n" +
                " |  ____/ ____|  /\\      | |   | |            \n" +
                " | |__ | (___   /  \\   __| | __| | ___  _ __  \n" +
                " |  __| \\___ \\ / /\\ \\ / _` |/ _` |/ _ \\| '_ \\ \n" +
                " | |    ____) / ____ \\ (_| | (_| | (_) | | | |\n" +
                " |_|   |_____/_/    \\_\\__,_|\\__,_|\\___/|_| |_|\n");

        getLogger().info("§7[§b!§7] Warming up...");

        max = getServer().getMaxPlayers();

        getServer().getPluginManager().registerEvents(new PingListener(this), this);

        schedule();

        getLogger().info("§7[§a!§7] Loaded successfully");
    }

    @Override
    public void onDisable() {

        getLogger().info("§7[§c!§7] Un-Loaded");

    }

    private void schedule() {
        task = getServer().getScheduler().runTaskTimer(this, () -> {

            if (isLocked) {
                task.cancel();
                return;
            }

            for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
                if (!plugin.isEnabled()) {
                    allPluginsLoaded = false;
                    break;
                }
            }

            if (allPluginsLoaded) {
                isLocked = true;
                task.cancel();
            }

        }, 20L, 40L);
    }

}
