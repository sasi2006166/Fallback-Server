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

            for (Plugin plugin : instance.getServer().getPluginManager().getPlugins()) {

                if (plugin.isEnabled()) {
                    continue;
                }

                if (tries >= 30) {
                    instance.getServer().getConsoleSender().sendMessage("[FallbackServerAddon] §7[§c!§7] Not all plugins are loaded, time's up.");
                    instance.setAllPluginsLoaded(true);
                    continue;
                }

                instance.setAllPluginsLoaded(false);
                tries++;
            }

            if (instance.isAllPluginsLoaded()) {
                instance.setLocked(true);
                instance.executeStart();
                tries = 0;
                task.cancel();
            }
        }, 20L, 40L);
    }
}
