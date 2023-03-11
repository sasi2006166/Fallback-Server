package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;

@UtilityClass
public class UpdateUtil {

    public void checkUpdates() {

        boolean isAlpha = FallbackServerBungee.getInstance().isAlpha();

        if (isAlpha) {
            return;
        }

        boolean updater = BungeeConfig.UPDATER.getBoolean();

        if (updater) {
            Utils.checkUpdates();
        }

    }
}
