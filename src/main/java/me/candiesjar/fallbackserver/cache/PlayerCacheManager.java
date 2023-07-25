package me.candiesjar.fallbackserver.cache;

import com.google.common.collect.Maps;
import me.candiesjar.fallbackserver.handler.FallbackLimboHandler;

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

    private final HashMap<UUID, FallbackLimboHandler> reconnectMap = Maps.newHashMap();

    public FallbackLimboHandler get(UUID key) {
        return reconnectMap.get(key);
    }

    public void put(UUID key, FallbackLimboHandler value) {
        reconnectMap.put(key, value);
    }

    public FallbackLimboHandler remove(UUID key) {
        return reconnectMap.remove(key);
    }

    public boolean containsKey(UUID key) {
        return reconnectMap.containsKey(key);
    }

}
