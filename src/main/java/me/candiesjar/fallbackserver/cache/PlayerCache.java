package me.candiesjar.fallbackserver.cache;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.candiesjar.fallbackserver.utils.tasks.ReconnectTask;

import java.util.Map;
import java.util.UUID;

@UtilityClass
public class PlayerCache {

    @Getter
    private final Map<UUID, ReconnectTask> reconnectMap = Maps.newHashMap();

}
