package me.candiesjar.fallbackserveraddon.utils;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.mrmicky.fastboard.FastBoard;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class ScoreboardUtil {

    private final HashMap<UUID, FastBoard> scoreboards = Maps.newHashMap();
    private MyScheduledTask taskboard;
    private final FallbackServerAddon instance = FallbackServerAddon.getInstance();

    public void createScoreboard(Player player) {
        UniversalScheduler.getScheduler(instance).runTaskLater(() -> {
            FastBoard board = new FastBoard(player);
            scoreboards.put(player.getUniqueId(), board);
        }, 20L);
    }

    public void deleteScoreboard(Player player) {
        if (scoreboards.get(player.getUniqueId()) != null) {
            scoreboards.get(player.getUniqueId()).delete();
            scoreboards.remove(player.getUniqueId());
        }
    }

    public void taskBoards() {
        taskboard = UniversalScheduler.getScheduler(instance).runTaskTimer(
                ScoreboardUtil::updateScoreboard,
                20L,
                instance.getConfig().getInt("settings.standalone.scoreboard.update_interval"));
    }

    public void reloadBoards() {
        if (taskboard != null) {
            taskboard.cancel();
            taskBoards();
            return;
        }
        taskBoards();
    }

    private void updateScoreboard() {
        instance.getServer().getOnlinePlayers().forEach(player -> {
            FastBoard board = scoreboards.get(player.getUniqueId());

            if (board == null) {
                return;
            }

            board.updateTitle(ChatUtil.color(player, instance.getConfig().getString("settings.standalone.scoreboard.title")));
            board.updateLines(parsePlaceholders(player, instance.getConfig().getStringList("settings.standalone.scoreboard.lines")));
        });
    }

    private List<String> parsePlaceholders(Player player, List<String> list) {
        List<String> parsedList = Lists.newArrayList();
        for (String s : list) {
            parsedList.add(ChatUtil.color(player, s));
        }
        return parsedList;
    }
}
