package com.synopsys.integration.alert.common;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SetMap<K, S> extends AbstractMap<K, Set<S>> {
    private final Map<K, Set<S>> map;

    public SetMap() {
        this(new HashMap());
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
    public Set<Entry<K, Set<S>>> entrySet() {
        return map.entrySet();
    }

    public Map<K, Set<S>> getMap() {
        return map;
    }

}
