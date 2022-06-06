package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.enums.BungeeConfig;

@UtilityClass
public class UpdateUtil {

    public void checkUpdates() {

        if (BungeeConfig.UPDATE_CHECKER.getBoolean()) {
            Utils.checkUpdates();
        }

    }
}
