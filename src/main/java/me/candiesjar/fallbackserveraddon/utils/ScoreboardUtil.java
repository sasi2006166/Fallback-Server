package me.candiesjar.fallbackserveraddon.utils;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.google.common.collect.Maps;
import fr.mrmicky.fastboard.FastBoard;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import me.candiesjar.fallbackserveraddon.utils.player.ChatUtil;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@UtilityClass
public class ScoreboardUtil {

    private final Map<UUID, FastBoard> scoreboards = Maps.newConcurrentMap();
    private final FallbackServerAddon instance = FallbackServerAddon.getInstance();
    private MyScheduledTask taskBoard;

    public void createScoreboard(Player player) {
        UniversalScheduler.getScheduler(instance).runTaskLater(() -> {
            FastBoard board = new FastBoard(player) {
                @Override
                public boolean hasLinesMaxLength() {
                    return true;
                }
            };
            scoreboards.put(player.getUniqueId(), board);
        }, 20L);
    }

    public void deleteScoreboard(Player player) {
        Optional.ofNullable(scoreboards.remove(player.getUniqueId()))
                .ifPresent(FastBoard::delete);
    }

    public void taskBoards() {
        taskBoard = UniversalScheduler.getScheduler(instance).runTaskTimer(
                ScoreboardUtil::updateScoreboards,
                20L,
                instance.getConfig().getInt("settings.standalone.scoreboard.update_interval"));
    }

    public void reloadBoards() {
        if (taskBoard != null) {
            taskBoard.cancel();
        }
        taskBoards();
    }

    private void updateScoreboards() {
        instance.getServer().getOnlinePlayers().forEach(player -> {
            FastBoard board = scoreboards.get(player.getUniqueId());

            if (board == null) {
                return;
            }

            updateBoard(player, board);
        });
    }

    private void updateBoard(Player player, FastBoard board) {
        String title = ChatUtil.color(player, instance.getConfig().getString("settings.standalone.scoreboard.title"));
        List<String> lines = parsePlaceholders(player, instance.getConfig().getStringList("settings.standalone.scoreboard.lines"));

        board.updateTitle(title);
        board.updateLines(lines);
    }

    private List<String> parsePlaceholders(Player player, List<String> list) {
        return list.stream()
                .map(s -> ChatUtil.color(player, s))
                .collect(Collectors.toList());
    }
}
