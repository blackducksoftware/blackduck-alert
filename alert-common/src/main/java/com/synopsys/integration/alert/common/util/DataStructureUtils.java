/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
