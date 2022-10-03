package com.synopsys.integration.alert.test.common.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.springframework.data.domain.Sort;

public class MockRepositorySorter<E> {

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
