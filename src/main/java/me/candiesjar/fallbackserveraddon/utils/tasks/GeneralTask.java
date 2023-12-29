package me.candiesjar.fallbackserveraddon.utils.tasks;

import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.plugin.Plugin;

@UtilityClass
public class GeneralTask {

    private MyScheduledTask task;
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
                instance.setAllPluginsLoaded(false);
            }

            if (instance.isAllPluginsLoaded()) {
                instance.setLocked(true);
                instance.executeStart();
                task.cancel();
            }

        }, 20L, 40L);
    }
}
