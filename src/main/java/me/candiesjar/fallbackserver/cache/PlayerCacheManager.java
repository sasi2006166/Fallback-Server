package me.candiesjar.fallbackserver.cache;

import me.candiesjar.fallbackserver.reconnect.server.ReconnectSession;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    private final ConcurrentHashMap<UUID, ReconnectSession> reconnectMap = new ConcurrentHashMap<>();

    public ReconnectSession get(UUID key) {
        return reconnectMap.get(key);
    }

    public ReconnectSession remove(UUID key) {
        return reconnectMap.remove(key);
    }

    public void addIfAbsent(UUID key, ReconnectSession value) {
        reconnectMap.putIfAbsent(key, value);
    }

    public boolean containsKey(UUID key) {
        return reconnectMap.containsKey(key);
    }

}
