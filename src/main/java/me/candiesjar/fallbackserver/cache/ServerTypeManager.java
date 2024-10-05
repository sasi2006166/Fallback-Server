package me.candiesjar.fallbackserver.cache;

import com.google.common.collect.Maps;
import lombok.Getter;
import me.candiesjar.fallbackserver.objects.ServerType;

import java.util.HashMap;

@Getter
public class ServerTypeManager {

    private static ServerTypeManager instance;

    public static ServerTypeManager getInstance() {
        if (instance == null) {
            instance = new ServerTypeManager();
        }
        return instance;
    }

    private ServerTypeManager() {

    }

    private final HashMap<String, ServerType> serverTypeMap = Maps.newHashMap();

    public ServerType get(String key) {
        return serverTypeMap.get(key);
    }

    public ServerType remove(String key) {
        return serverTypeMap.remove(key);
    }

    public void put(String key, ServerType value) {
        serverTypeMap.put(key, value);
    }

    public void clear() {
        serverTypeMap.clear();
    }

}
