package me.candiesjar.fallbackserver.cache;

import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.handlers.FallbackReconnectHandler;

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

    private final HashMap<UUID, FallbackReconnectHandler> reconnectMap = Maps.newHashMap();

    public FallbackReconnectHandler get(UUID key) {
        return reconnectMap.get(key);
    }

    public FallbackReconnectHandler remove(UUID key) {
        return reconnectMap.remove(key);
    }

    public void put(UUID key, FallbackReconnectHandler value) {
        reconnectMap.put(key, value);
    }

    public boolean containsKey(UUID key) {
        return reconnectMap.containsKey(key);
    }

}
