package me.candiesjar.fallbackserver.cache;

import com.google.common.collect.Maps;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.HashMap;

public class ServerCacheManager {

    private static ServerCacheManager instance;

    public static ServerCacheManager getInstance() {
        if (instance == null) {
            instance = new ServerCacheManager();
        }
        return instance;
    }

    private ServerCacheManager() {

    }

    private final HashMap<String, ServerInfo> serversMap = Maps.newHashMap();

    public ServerInfo get(String key) {
        return serversMap.get(key);
    }

    public boolean containsKey(String key) {
        return serversMap.containsKey(key);
    }

    public void put(String key, ServerInfo value) {
        serversMap.put(key, value);
    }

    public void remove(String key) {
        serversMap.remove(key);
    }

    public void clear() {
        serversMap.clear();
    }

}
