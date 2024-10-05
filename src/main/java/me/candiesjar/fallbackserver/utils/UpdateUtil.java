package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.enums.BungeeConfig;

@UtilityClass
public class UpdateUtil {

    public void checkUpdates() {
        boolean updater = BungeeConfig.UPDATER.getBoolean();

        if (updater) {
            Utils.checkUpdates();
        }

    }
}
