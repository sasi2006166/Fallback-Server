package me.candiesjar.fallbackserver.cache;

import com.google.common.collect.Maps;
import lombok.Getter;
import me.candiesjar.fallbackserver.FallbackServerVelocity;
import me.candiesjar.fallbackserver.enums.VelocityConfig;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ServerCacheManager {

    private static ServerCacheManager instance;
    private final FallbackServerVelocity fallbackServerVelocity = FallbackServerVelocity.getInstance();

    public static ServerCacheManager getInstance() {
        if (instance == null) {
            instance = new ServerCacheManager();
        }
        return instance;
    }

    private ServerCacheManager() {

    }

    @Getter
    private final HashMap<String, Integer> servers = Maps.newHashMap();

    public Integer get(String key) {
        return servers.get(key);
    }

    public void put(String key, Integer value) {
        servers.put(key, value);
        schedule(key);
    }

    public void remove(String key) {
        servers.remove(key);
    }

    public void removeIfContains(String key) {
        if (containsKey(key)) {
            remove(key);
        }
    }

    public boolean containsKey(String key) {
        return servers.containsKey(key);
    }

    private void schedule(String key) {
        fallbackServerVelocity.getServer().getScheduler().buildTask(fallbackServerVelocity, () -> remove(key))
                .delay(VelocityConfig.RECONNECT_SOCKET_TASK.get(Integer.class) / 2, TimeUnit.SECONDS).schedule();
    }

}
