package me.candiesjar.fallbackserver.utils.checks;

import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import org.simpleyaml.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class OutdatedChecks {

    private FallbackServerVelocity getInstance() {
        return FallbackServerVelocity.getInstance();
    }

    private final Configuration config = getInstance().getConfigTextFile().getConfig();

    public void handle() {
        boolean isOutdated =
                !config.getStringList("fallback_list").isEmpty() ||
                        config.getString("settings.ping_mode") == null ||
                        !config.getStringList("fallback_mode").isEmpty();

        if (isOutdated) {
            setOutdated();
        }
    }

    private void setOutdated() {
        getInstance().setOutdated(true);
    }

}
