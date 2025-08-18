package me.candiesjar.fallbackserver.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;

@Getter
public class OnlineLobbiesManager {

    private static OnlineLobbiesManager instance;

    public static OnlineLobbiesManager getInstance() {
        if (instance == null) {
            instance = new OnlineLobbiesManager();
        }
        return instance;
    }

    private OnlineLobbiesManager() {


    }

    private final HashMap<String, List<RegisteredServer>> onlineLobbies = Maps.newHashMap();

    public List<RegisteredServer> get(String key) {
        if (!onlineLobbies.containsKey(key)) {
            onlineLobbies.put(key, Lists.newArrayList());
        }
        return onlineLobbies.get(key);
    }

    public void put(String key, RegisteredServer registeredServer) {
        List<RegisteredServer> serverInfos = onlineLobbies.get(key);
        serverInfos.add(registeredServer);
        onlineLobbies.put(key, serverInfos);
    }

    public void remove(String key, RegisteredServer registeredServer) {
        List<RegisteredServer> serverInfos = onlineLobbies.get(key);
        serverInfos.remove(registeredServer);
        onlineLobbies.put(key, serverInfos);
    }

    public boolean containsValue(String key, RegisteredServer registeredServer) {
        List<RegisteredServer> serverInfos = onlineLobbies.get(key);
        return serverInfos.contains(registeredServer);
    }

    public void firstLoad(String key) {
        onlineLobbies.put(key, Lists.newArrayList());
    }

    public void clear() {
        onlineLobbies.clear();
    }

}
