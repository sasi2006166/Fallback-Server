package me.candiesjar.fallbackserveraddon.utils;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

@UtilityClass
public class BossBarUtil {

    private final FallbackServerAddon plugin = FallbackServerAddon.getInstance();
    private final TaskScheduler scheduler = UniversalScheduler.getScheduler(plugin);

    public void sendBossBar(Player player, String message, String barColor, String barStyle, double progress) {
        if (validVersion()) {
            BossBar bossBar = plugin.getServer().createBossBar(message, parseBarColor(barColor), parseBarStyle(barStyle));
            bossBar.setProgress(progress);
            bossBar.addPlayer(player);
            scheduler.runTaskTimer(() -> updateBossBar(player, message), 20L, 20L);
        }
    }

    public void removeBossBar(Player player) {
        if (validVersion() && plugin.getConfig().getBoolean("settings.standalone.bossbar.enabled", false)) {
            plugin.getServer().getBossBars().forEachRemaining(bar -> {
                if (bar.getPlayers().contains(player)) {
                    bar.removePlayer(player);
                }
            });
        }
    }

    private void updateBossBar(Player player, String message) {
        if (validVersion()) {
            plugin.getServer().getBossBars().forEachRemaining(bar -> {
                if (bar.getPlayers().contains(player)) {
                    bar.setTitle(message);
                }
            });
        }
    }

    private BarColor parseBarColor(String barColor) {
        switch (barColor) {
            case "BLUE":
                return BarColor.BLUE;
            case "GREEN":
                return BarColor.GREEN;
            case "PINK":
                return BarColor.PINK;
            case "PURPLE":
                return BarColor.PURPLE;
            case "RED":
                return BarColor.RED;
            case "YELLOW":
                return BarColor.YELLOW;
            default:
                return BarColor.WHITE;
        }
    }

    private BarStyle parseBarStyle(String barStyle) {
        switch (barStyle) {
            case "SEGMENTED_6":
                return BarStyle.SEGMENTED_6;
            case "SEGMENTED_10":
                return BarStyle.SEGMENTED_10;
            case "SEGMENTED_12":
                return BarStyle.SEGMENTED_12;
            case "SEGMENTED_20":
                return BarStyle.SEGMENTED_20;
            default:
                return BarStyle.SOLID;
        }
    }

    private boolean validVersion() {
        String nmsVersion = plugin.getServer().getClass().getPackage().getName();
        nmsVersion = nmsVersion.substring(nmsVersion.lastIndexOf(".") + 1);
        return !nmsVersion.equalsIgnoreCase("v1_8_R1") && !nmsVersion.startsWith("v1_7_");
    }
}
