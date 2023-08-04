package me.candiesjar.fallbackserveraddon;

import lombok.Setter;
import lombok.SneakyThrows;
import me.candiesjar.fallbackserveraddon.listeners.PingListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class FallbackServerAddon extends JavaPlugin {

    @Setter
    private boolean allPluginsLoaded = true;
    private boolean locked = false;

    private BukkitTask task;

    @Override
    public void onEnable() {

        getServer().getConsoleSender().sendMessage("\n" +
                "  ______ _____            _     _             \n" +
                " |  ____/ ____|  /\\      | |   | |            \n" +
                " | |__ | (___   /  \\   __| | __| | ___  _ __  \n" +
                " |  __| \\___ \\ / /\\ \\ / _` |/ _` |/ _ \\| '_ \\ \n" +
                " | |    ____) / ____ \\ (_| | (_| | (_) | | | |\n" +
                " |_|   |_____/_/    \\_\\__,_|\\__,_|\\___/|_| |_|\n");

        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§b!§7] Warming up...");

        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new PingListener(this), this);
        schedule();

        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§a!§7] Loaded successfully.");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] Un-Loaded.");
    }

    private void schedule() {
        task = getServer().getScheduler().runTaskTimer(this, () -> {

            if (locked) {
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
                locked = true;
                task.cancel();
            }

        }, 20L, 40L);
    }

    public boolean isLocked() {
        return locked;
    }
}
