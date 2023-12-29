package me.candiesjar.fallbackserveraddon.utils;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.google.common.collect.Maps;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

@UtilityClass
public class ActionBarUtil {

    private final HashMap<UUID, BukkitTask> actionbarTask = Maps.newHashMap();
    private final HashMap<UUID, WrappedTask> actionbarFoliaTask = Maps.newHashMap();
    private final FallbackServerAddon instance = FallbackServerAddon.getInstance();

    public void startActionBar(Player player, String message) {
        if (Utils.isFolia()) {
            startActionBarFolia(player, message);
            return;
        }

        actionbarTask.put(player.getUniqueId(), instance.getServer().getScheduler().runTaskTimer(instance, () -> sendActionBar(player, message), 20, 20));
    }

    public void stopActionBar(Player player) {
        if (Utils.isFolia()) {
            stopActionBarFolia(player);
            return;
        }

        if (actionbarTask.containsKey(player.getUniqueId())) {
            actionbarTask.get(player.getUniqueId()).cancel();
            actionbarTask.remove(player.getUniqueId());
        }
    }

    private void stopActionBarFolia(Player player) {
        if (actionbarFoliaTask.containsKey(player.getUniqueId())) {
            actionbarFoliaTask.get(player.getUniqueId()).cancel();
            actionbarFoliaTask.remove(player.getUniqueId());
        }
    }

    private void startActionBarFolia(Player player, String message) {
        FoliaLib foliaLib = new FoliaLib(instance);
        actionbarFoliaTask.put(player.getUniqueId(), foliaLib.getImpl().runTimer(() -> sendActionBar(player, message), 20, 20));
    }

    private void sendActionBar(Player player, String message) {
        ActionBarAPI.sendActionBar(player, ChatUtil.color(message));
    }
}
