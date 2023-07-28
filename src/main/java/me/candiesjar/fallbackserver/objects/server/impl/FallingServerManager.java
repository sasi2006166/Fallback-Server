package me.candiesjar.fallbackserver.objects.server.impl;

import com.google.common.collect.Maps;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.candiesjar.fallbackserver.objects.server.ObjectManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class FallingServerManager implements ObjectManager<String, RegisteredServer> {
    private final HashMap<String, RegisteredServer> cache = Maps.newHashMap();

    @Override
    public void add(String key, RegisteredServer value) {
        cache.put(key.toLowerCase(), value);
    }

    @Override
    public void remove(String key) {
        cache.remove(key.toLowerCase());
    }

    @Override
    public Optional<RegisteredServer> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public Collection<RegisteredServer> getAll() {
        return cache.values();
    }

    public void clearCache() {
        cache.clear();
    }

}
