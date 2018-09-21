/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.workflow.filter.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.common.field.ObjectHierarchicalField;
import com.synopsys.integration.alert.common.field.StringHierarchicalField;

public class JsonFieldAccessor {
    private Map<StringHierarchicalField, List<String>> stringMappings = new HashMap<>();
    private Map<ObjectHierarchicalField, List<Object>> objectMappings = new HashMap<>();

    public JsonFieldAccessor(final Map<HierarchicalField, List<Object>> fieldToDataMap) {
        initializeSubMap(fieldToDataMap, stringMappings, StringHierarchicalField.class);
        initializeSubMap(fieldToDataMap, objectMappings, ObjectHierarchicalField.class);
    }

    public List<String> get(final StringHierarchicalField field) {
        return getFrom(stringMappings, field);
    }

    public List<Object> get(final ObjectHierarchicalField field) {
        return getFrom(objectMappings, field);
    }

    public <T> List<T> get(final ObjectHierarchicalField field, final Class<T> expectedClazz) throws AlertException {
        final List<Object> objectList = get(field);

        final List<T> typedList = new ArrayList<>();
        for (final Object object : objectList) {
            if (expectedClazz.isAssignableFrom(object.getClass())) {
                typedList.add(expectedClazz.cast(object));
            } else {
                throw new AlertException(String.format("Invalid data type '%s' for field '%s'.", expectedClazz.getName(), field.getLabel()));
            }
        }
        return typedList;
    }

    public Optional<String> getFirst(final StringHierarchicalField field) {
        return getFirst(get(field));
    }

    public Optional<Object> getFirst(final ObjectHierarchicalField field) {
        return getFirst(get(field));
    }

    public <T> Optional<T> getFirst(final ObjectHierarchicalField field, final Class<T> expectedClass) throws AlertException {
        return getFirst(get(field, expectedClass));
    }

    private <K, V> List<V> getFrom(final Map<K, List<V>> map, final K field) {
        if (map.containsKey(field)) {
            return map.get(field);
        }
        return Collections.emptyList();
    }

    private <K, V> Optional<V> getFirst(final List<V> list) {
        if (!list.isEmpty()) {
            final V firstValue = list.get(0);
            return Optional.of(firstValue);
        }
        return Optional.empty();
    }

    private <K, V> void initializeSubMap(final Map<HierarchicalField, List<Object>> fieldToDataMap, Map<K, List<V>> subMap, final Class<K> targetKeyClass) {
        for (final Map.Entry<HierarchicalField, List<Object>> entry : fieldToDataMap.entrySet()) {
            final HierarchicalField key = entry.getKey();
            if (targetKeyClass.isAssignableFrom(key.getClass())) {
                final List<V> newValues = new ArrayList<>();

                final List<Object> values = entry.getValue();
                for (final Object value : values) {
                    newValues.add((V) value);
                }
                subMap.put((K) key, newValues);
            }
        }

    }
}
