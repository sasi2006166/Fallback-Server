package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.enums.VelocityConfig;

@UtilityClass
public class UpdateUtil {

    public void checkUpdates() {
        if (VelocityConfig.UPDATE_CHECKER.get(Boolean.class)) {
            VelocityUtils.getUpdates();
        }




    }
}
