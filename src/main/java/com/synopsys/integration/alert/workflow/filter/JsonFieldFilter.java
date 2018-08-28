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
package com.synopsys.integration.alert.workflow.filter;

import java.util.List;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.database.entity.NotificationContent;

public class JsonFieldFilter implements JsonFilterBuilder {
    private final Gson gson;
    private final List<String> fieldNameHierarchy;
    private final String value;

    public JsonFieldFilter(final Gson gson, final HierarchicalField hierarchicalField, final String value) {
        this.gson = gson;
        this.value = value;
        this.fieldNameHierarchy = hierarchicalField.getFullPathToField();
    }

    @Override
    public Predicate<NotificationContent> buildPredicate() {
        final Predicate<NotificationContent> contentPredicate = (notification) -> {
            final JsonObject object = gson.fromJson(notification.getContent(), JsonObject.class);

            final JsonElement foundObject = getFieldContainingValue(object, fieldNameHierarchy.get(0), 1);
            return foundObject != null;
        };
        return contentPredicate;
    }

    private JsonElement getFieldContainingValue(final JsonElement jsonElement, final String fieldName, final int nextIndex) {
        if (jsonElement != null) {
            final JsonElement jsonField = getFieldValue(jsonElement, fieldName, nextIndex);

            // check if we are at the expected depth
            if (nextIndex < fieldNameHierarchy.size()) {
                return getFieldContainingValue(jsonField, fieldNameHierarchy.get(nextIndex), nextIndex + 1);
            } else if (jsonField != null && jsonField.isJsonPrimitive()) {
                final JsonPrimitive jsonPrimitive = jsonField.getAsJsonPrimitive();
                if (jsonPrimitive.isString() && value.equals(jsonPrimitive.getAsString())) {
                    return jsonField;
                }
            }
        }
        return null;
    }

    private JsonElement getFieldValue(final JsonElement jsonElement, final String fieldName, final int nextIndex) {
        if (jsonElement.isJsonObject()) {
            final JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.get(fieldName);
        } else if (jsonElement.isJsonArray()) {
            // TODO we might be able to parallelize this
            for (final JsonElement arrayElement : jsonElement.getAsJsonArray()) {
                // search each element of the array; if something matches, we're done
                final JsonElement jsonField = getFieldContainingValue(arrayElement, fieldName, nextIndex + 1);
                if (jsonField != null) {
                    return jsonField;
                }
            }
        }
        return null;
    }
}
