package me.candiesjar.fallbackserver.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.md_5.bungee.api.config.ServerInfo;

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

    private final HashMap<String, List<ServerInfo>> onlineLobbies = Maps.newHashMap();

    public List<ServerInfo> get(String key) {
        if (!onlineLobbies.containsKey(key)) {
            onlineLobbies.put(key, Lists.newArrayList());
        }

        return onlineLobbies.get(key);
    }

    public void put(String key, ServerInfo serverInfo) {
        List<ServerInfo> serverInfos = onlineLobbies.get(key);
        serverInfos.add(serverInfo);
        onlineLobbies.put(key, serverInfos);
    }

    public void remove(String key, ServerInfo serverInfo) {
        List<ServerInfo> serverInfos = onlineLobbies.get(key);
        serverInfos.remove(serverInfo);
        onlineLobbies.put(key, serverInfos);
    }

    public boolean containsValue(String key, ServerInfo serverInfo) {
        List<ServerInfo> serverInfos = onlineLobbies.get(key);
        return serverInfos.contains(serverInfo);
    }

    public void firstLoad(String key) {
        onlineLobbies.put(key, Lists.newArrayList());
    }

    public void clear() {
        onlineLobbies.clear();
    }

}
