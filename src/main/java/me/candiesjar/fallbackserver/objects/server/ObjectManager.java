package me.candiesjar.fallbackserver.objects.server;

import java.util.Collection;
import java.util.Optional;

public interface ObjectManager<K, V> {

    void add(K key, V value);

    void remove(K key);

    Optional<V> get(K key);

    Collection<V> getAll();
}
