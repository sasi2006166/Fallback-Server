package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerBungee;
import me.candiesjar.fallbackserver.enums.BungeeConfig;

@UtilityClass
public class UpdateUtil {

    private final FallbackServerBungee fallbackServerBungee = FallbackServerBungee.getInstance();

    public void checkUpdates() {
        boolean isBeta = fallbackServerBungee.isBeta();

        if (isBeta) {
            return;
        }

        boolean updater = BungeeConfig.UPDATER.getBoolean();

        if (updater) {
            Utils.checkUpdates();
        }

    }
}
