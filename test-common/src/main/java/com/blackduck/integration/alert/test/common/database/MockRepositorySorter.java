/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.test.common.database;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;

public class MockRepositorySorter<E> {
    public static <E, U extends Comparable<? super U>> UnaryOperator<List<E>> createSingleFieldSorter(Function<? super E, ? extends U> keyExtractor) {
        return entityList -> entityList.stream()
            .sorted(Comparator.comparing(keyExtractor))
            .collect(Collectors.toList());
    }

    private final Map<String, UnaryOperator<List<E>>> fieldSorters = new HashMap<>();

    public final List<E> sort(Sort sort, List<E> values) {
        Optional<Sort.Order> orderOptional = sort.get().findFirst();
        if (orderOptional.isEmpty()) {
            return values;
        }

        Sort.Order order = orderOptional.get();
        String property = order.getProperty();
        Sort.Direction direction = order.getDirection();
        if (!fieldSorters.containsKey(property)) {
            return values;
        }
        List<E> sortedList = fieldSorters.get(property).apply(values);
        if (direction.isDescending()) {
            Collections.reverse(sortedList);
        }
        return sortedList;
    }

    public void applyFieldSorter(String fieldName, UnaryOperator<List<E>> fieldSorter) {
        fieldSorters.put(fieldName, fieldSorter);
    }
}
