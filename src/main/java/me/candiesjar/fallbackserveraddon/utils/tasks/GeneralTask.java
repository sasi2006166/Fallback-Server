package me.candiesjar.fallbackserveraddon.utils.tasks;

import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.plugin.Plugin;

@UtilityClass
public class GeneralTask {

    private MyScheduledTask task;
    private int tries = 0;

    public void schedule(FallbackServerAddon instance, TaskScheduler scheduler) {
        task = scheduler.runTaskTimer(() -> {
            if (instance.isLocked()) {
                task.cancel();
                return;
            }

            if (!checkPlugins(instance)) {
                instance.setAllPluginsLoaded(false);
                tries++;
            } else {
                instance.setAllPluginsLoaded(true);
            }

            if (tries >= instance.getConfig().getInt("settings.addon.max_start_tries")) {
                notifyTimeUp(instance);
                finalizeStartup(instance);
            }

            if (instance.isAllPluginsLoaded()) {
                finalizeStartup(instance);
            }
        }, 20L, 40L);
    }

    private boolean checkPlugins(FallbackServerAddon instance) {
        for (Plugin plugin : instance.getServer().getPluginManager().getPlugins()) {
            if (!plugin.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    private void notifyTimeUp(FallbackServerAddon instance) {
        instance.getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] Not all plugins are loaded, time's up.");
        instance.getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] Some other plugins are having abnormal behavior.");
        instance.getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] The startup will be attempted anyway.");
    }

    private void finalizeStartup(FallbackServerAddon instance) {
        instance.setLocked(true);
        instance.executeStart();
        tries = 0;
        task.cancel();
    }
}
