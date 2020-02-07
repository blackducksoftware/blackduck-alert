/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
