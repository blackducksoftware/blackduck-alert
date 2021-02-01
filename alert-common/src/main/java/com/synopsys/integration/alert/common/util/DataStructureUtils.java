/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.util;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class DataStructureUtils {
    public static <K, V> Map<K, V> mapToValues(Collection<V> valueCollection, Function<V, K> keyExtractor) {
        return mapToMap(valueCollection, keyExtractor, Function.identity());
    }

    public static <K, V> Map<K, V> mapToKeys(Collection<K> valueCollection, Function<K, V> valueExtractor) {
        return mapToMap(valueCollection, Function.identity(), valueExtractor);
    }

    public static <K, V, C> Map<K, V> mapToMap(Collection<C> valueCollection, Function<C, K> keyExtractor, Function<C, V> valueExtractor) {
        return valueCollection.stream().collect(Collectors.toMap(keyExtractor, valueExtractor));
    }

    public static <T, E extends Enum<E>> Collector<T, ?, EnumMap<E, T>> toEnumMap(Function<? super T, ? extends E> keyMapper, Class<E> enumClass) {
        return toEnumMap(keyMapper, Function.identity(), enumClass);
    }

    public static <T, E extends Enum<E>, V> Collector<T, ?, EnumMap<E, V>> toEnumMap(Function<? super T, ? extends E> keyMapper, Function<? super T, ? extends V> valueMapper, Class<E> enumClass) {
        return Collectors.toMap(keyMapper, valueMapper, (e1, e2) -> e2, () -> new EnumMap<>(enumClass));
    }

    private DataStructureUtils() {
    }

}
