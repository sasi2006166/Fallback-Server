package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;

@UtilityClass
public class UpdateUtil {

    public void checkUpdates() {

        if (FallbackServerBungee.getInstance().isAlpha()) {
            FallbackServerBungee.getInstance().getLogger().info("§7Updater is disabled in alpha version(s).");
            FallbackServerBungee.getInstance().getLogger().info(" ");
            return;
        }

        if (BungeeConfig.UPDATER.getBoolean()) {
            Utils.checkUpdates();
        }

    }
}
