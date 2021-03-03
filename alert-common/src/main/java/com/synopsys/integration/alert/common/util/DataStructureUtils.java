/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DataStructureUtils {
    public static <K, V> Map<K, V> mapToValues(Collection<V> valueCollection, Function<V, K> keyExtractor) {
        return mapToMap(valueCollection, keyExtractor, Function.identity());
    }

    public static <K, V> Map<K, V> mapToKeys(Collection<K> valueCollection, Function<K, V> valueExtractor) {
        return mapToMap(valueCollection, Function.identity(), valueExtractor);
    }

    public static <K, V, C> Map<K, V> mapToMap(Collection<C> valueCollection, Function<C, K> keyExtractor, Function<C, V> valueExtractor) {
        return valueCollection.stream().collect(Collectors.toMap(keyExtractor::apply, valueExtractor::apply));
    }

    private DataStructureUtils() {
    }
}
