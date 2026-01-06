package com.blackduck.integration.alert.common.persistence.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class SizeLimitedMap<K,V> extends LinkedHashMap<K,V> {
    private final int limit;

    public SizeLimitedMap() {
        this(10);
    }
    public SizeLimitedMap(int limit) {
        this.limit = limit;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
        return size() > limit;
    }
}
