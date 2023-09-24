package me.candiesjar.fallbackserver.utils;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.enums.BungeeConfig;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;

import java.util.List;

@UtilityClass
public class CheckUtil {

    public boolean preChecks(ProxiedPlayer player, ServerKickEvent.State state, String reason, boolean reconnect) {

        if (!player.isConnected()) {
            return true;
        }

        if (state != ServerKickEvent.State.CONNECTED) {
            return true;
        }

        if (reason != null) {
            if (reconnect) {
                return checkReason(BungeeConfig.RECONNECT_IGNORED_REASONS.getStringList(), reason);
            } else {
                return checkReason(BungeeConfig.IGNORED_REASONS.getStringList(), reason);
            }
        }

        return false;

    }

    private boolean checkReason(List<String> ignoredReasons, String reason) {

        for (String word : ignoredReasons) {

            if (reason.contains(word)) {
                return true;
            }

        }

        return false;

    }

}

