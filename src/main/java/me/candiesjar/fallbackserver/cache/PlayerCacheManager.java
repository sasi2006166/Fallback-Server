package me.candiesjar.fallbackserver.cache;

import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.utils.tasks.ReconnectTask;

import java.util.HashMap;
import java.util.UUID;

public class PlayerCacheManager {

    private static PlayerCacheManager instance;

    public static PlayerCacheManager getInstance() {
        if (instance == null) {
            instance = new PlayerCacheManager();
        }
        return instance;
    }

    private PlayerCacheManager() {

    }

    private final HashMap<UUID, ReconnectTask> reconnectMap = Maps.newHashMap();

    public ReconnectTask get(UUID key) {
        return reconnectMap.get(key);
    }

    public void put(UUID key, ReconnectTask value) {
        reconnectMap.put(key, value);
    }

    public boolean containsKey(UUID key) {
        return reconnectMap.containsKey(key);
    }

    public ReconnectTask remove(UUID key) {
        return reconnectMap.remove(key);
    }

}
