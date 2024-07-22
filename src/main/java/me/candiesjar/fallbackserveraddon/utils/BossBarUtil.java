package me.candiesjar.fallbackserveraddon.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserveraddon.FallbackServerAddon;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

@UtilityClass
public class BossBarUtil {

    private final FallbackServerAddon plugin = FallbackServerAddon.getInstance();

    public void sendBossBar(Player player, String message, String barColor, String barStyle, double progress) {
        if (validVersion()) {
            BossBar bar = plugin.getServer().createBossBar(message, parseBarColor(barColor), parseBarStyle(barStyle));
            bar.setProgress(progress);
            bar.addPlayer(player);
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
