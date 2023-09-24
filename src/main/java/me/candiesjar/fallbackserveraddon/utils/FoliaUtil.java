package me.candiesjar.fallbackserveraddon.utils;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.plugin.Plugin;

@UtilityClass
public class FoliaUtil {

    private final FallbackServerAddon instance = FallbackServerAddon.getInstance();
    private WrappedTask foliaTask;

    public void schedule() {
        FoliaLib foliaLib = new FoliaLib(instance);
        foliaTask = foliaLib.getImpl().runTimer(() -> {

            if (instance.isLocked()) {
                foliaTask.cancel();
                return;
            }

            for (Plugin plugin : instance.getServer().getPluginManager().getPlugins()) {
                if (!plugin.isEnabled()) {
                    instance.setAllPluginsLoaded(false);
                    break;
                }
            }

            if (instance.isAllPluginsLoaded()) {
                instance.setLocked(true);
                instance.executeStart();
                foliaTask.cancel();
            }

        }, 20L, 40L);
    }

}
