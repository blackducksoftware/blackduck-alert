package com.synopsys.integration.alert.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SetMap<K, S> implements Map<K, Set<S>> {
    private final Map<K, Set<S>> map;

    public SetMap() {
        this.map = new HashMap();
    }

    public SetMap(final Map<K, Set<S>> map) {
        this.map = map;
    }

    public Set<S> getValue(K key) {
        return map.get(key);
    }

    public Set<S> add(K key, S value) {
        if (map.containsKey(key)) {
            Set<S> set = map.get(key);
            set.add(value);
            return map.put(key, set);
        } else {
            Set<S> set = new HashSet<>();
            set.add(value);
            return map.put(key, set);
        }
    }

    public Set<S> addAll(K key, Set<S> value) {
        if (map.containsKey(key)) {
            Set<S> set = map.get(key);
            set.addAll(value);
            return map.put(key, set);
        } else {
            Set<S> set = new HashSet<>();
            set.addAll(value);
            return map.put(key, set);
        }
    }

    @Override
    public Set<S> put(final K key, final Set<S> value) {
        return map.put(key, value);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends Set<S>> mapToAdd) {
        map.putAll(mapToAdd);
    }

    @Override
    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<S> get(final Object key) {
        return map.get(key);
    }

    @Override
    public Set<S> remove(final Object key) {
        return map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Set<S>> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, Set<S>>> entrySet() {
        return map.entrySet();
    }

    public Map<K, Set<S>> getMap() {
        return map;
    }

}
