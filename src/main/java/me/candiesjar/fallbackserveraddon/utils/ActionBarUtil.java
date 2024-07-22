package me.candiesjar.fallbackserveraddon.utils;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.utils.nms.ActionBarCreator;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

@UtilityClass
public class ActionBarUtil {

    private final HashMap<UUID, MyScheduledTask> actionbarTask = Maps.newHashMap();
    private final FallbackServerAddon instance = FallbackServerAddon.getInstance();

    public void startActionBar(Player player, String message) {
        actionbarTask.put(player.getUniqueId(), UniversalScheduler.getScheduler(instance).runTaskTimer(() ->
                sendActionBar(player, message), 20L, 20L));
    }

    public void stopActionBar(Player player) {
        if (actionbarTask.containsKey(player.getUniqueId())) {
            actionbarTask.get(player.getUniqueId()).cancel();
            actionbarTask.remove(player.getUniqueId());
        }
    }

    private void sendActionBar(Player player, String message) {
        ActionBarCreator.sendActionBar(player, ChatUtil.color(player, message), ActionBarUtil.instance);
    }
}
