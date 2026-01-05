package com.blackduck.integration.alert.common.persistence.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class AccessorLimitedMap<K,V> extends LinkedHashMap<K,V> {
    private final int limit;

    public AccessorLimitedMap() {
        this(10);
    }
    public AccessorLimitedMap(int limit) {
        this.limit = limit;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
        return size() > limit;
    }
}
