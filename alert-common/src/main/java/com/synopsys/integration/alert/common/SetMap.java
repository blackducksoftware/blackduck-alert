package com.synopsys.integration.alert.common;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
        Set<S> set = this.computeIfAbsent(key, ignoredKey -> new LinkedHashSet<>());
        set.add(value);
        return set;
    }

    public Set<S> addAll(K key, Set<S> value) {
        Set<S> set = this.computeIfAbsent(key, ignoredKey -> new LinkedHashSet<>());
        set.addAll(value);
        return set;
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
