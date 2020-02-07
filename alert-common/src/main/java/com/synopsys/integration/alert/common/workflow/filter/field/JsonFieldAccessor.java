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
package com.synopsys.integration.alert.common.workflow.filter.field;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonFieldAccessor {
    private final Map<JsonField, List<Object>> fieldToDataMap;

    public JsonFieldAccessor(final Map<JsonField, List<Object>> fieldToDataMap) {
        this.fieldToDataMap = fieldToDataMap;
    }

    public <T> List<T> get(final JsonField<T> field) {
        final List<T> foundValues = (List<T>) fieldToDataMap.get(field);
        if (foundValues != null) {
            return foundValues;
        }
        return Collections.emptyList();
    }

    public <T> Optional<T> getFirst(final JsonField<T> field) {
        return getFirst(get(field));
    }

    private <V> Optional<V> getFirst(final List<V> list) {
        if (!list.isEmpty()) {
            final V firstValue = list.get(0);
            return Optional.of(firstValue);
        }
        return Optional.empty();
    }
}
