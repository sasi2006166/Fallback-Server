package me.candiesjar.fallbackserver.cache;

import com.google.common.collect.Maps;
import lombok.Getter;
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

    @Getter
    private final HashMap<ServerInfo, Boolean> servers = Maps.newHashMap();

    public Boolean get(ServerInfo key) {
        return servers.get(key);
    }

    public void put(ServerInfo key, Boolean value) {
        servers.put(key, value);
    }

    public boolean containsKey(ServerInfo key) {
        return servers.containsKey(key);
    }

    public Boolean remove(ServerInfo key) {
        return servers.remove(key);
    }

    public void clear() {
        servers.clear();
    }

}
