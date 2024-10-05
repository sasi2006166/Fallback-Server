package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ConditionUtil {
    public boolean checkReason(List<String> ignoredReasons, String reason) {
        if (reason == null || ignoredReasons == null) {
            return false;
        }

        for (String word : ignoredReasons) {

            if (reason.contains(word)) {
                return true;
            }

        }
        return false;
    }
}
