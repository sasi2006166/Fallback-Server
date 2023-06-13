package me.candiesjar.fallbackserver.cache;

import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.handlers.ReconnectHandler;

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

    private final HashMap<UUID, ReconnectHandler> reconnectMap = Maps.newHashMap();

    public ReconnectHandler get(UUID key) {
        return reconnectMap.get(key);
    }

    public void put(UUID key, ReconnectHandler value) {
        reconnectMap.put(key, value);
    }

    public boolean containsKey(UUID key) {
        return reconnectMap.containsKey(key);
    }

    public ReconnectHandler remove(UUID key) {
        return reconnectMap.remove(key);
    }

}
