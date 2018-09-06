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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.web.model.Config;

@Component
public class JsonExtractor {
    private final Gson gson;

    @Autowired
    public JsonExtractor(final Gson gson) {
        this.gson = gson;
    }

    public Optional<String> getFirstValueFromJson(final HierarchicalField hierarchicalField, final String json) {
        final List<String> foundValues = getValuesFromJson(hierarchicalField, json);
        if (!foundValues.isEmpty()) {
            Optional.of(foundValues.get(0));
        }
        return Optional.empty();
    }

    public List<String> getValuesFromJson(final HierarchicalField hierarchicalField, final String json) {
        final List<String> fieldNameHierarchy = hierarchicalField.getFullPathToField();
        final JsonObject object = gson.fromJson(json, JsonObject.class);

        final JsonElement foundElement = getFieldContainingValue(object, fieldNameHierarchy, fieldNameHierarchy.get(0), 1);
        return getValuesFromElement(foundElement, hierarchicalField.getFieldKey());
    }

    public List<String> getValuesFromConfig(final String fieldKey, final Config config) {
        final JsonObject jsonConfig = gson.toJsonTree(config).getAsJsonObject();
        return getValuesFromElement(jsonConfig, fieldKey);
    }

    private JsonElement getFieldContainingValue(final JsonElement jsonElement, final List<String> fieldNameHierarchy, final String fieldName, final int nextIndex) {
        if (jsonElement != null && jsonElement.isJsonObject()) {
            final JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonElement jsonField = jsonObject.get(fieldName);
            if (jsonField != null) {
                if (nextIndex == fieldNameHierarchy.size() - 1 && jsonField.isJsonArray()) {
                    return jsonField;
                } else if (nextIndex < fieldNameHierarchy.size()) {
                    return getFieldContainingValue(jsonField, fieldNameHierarchy, fieldNameHierarchy.get(nextIndex), nextIndex + 1);
                } else if (jsonField.isJsonPrimitive()) {
                    return jsonField.getAsJsonPrimitive();
                }
            }
        }
        return JsonNull.INSTANCE;
    }

    private List<String> getValuesFromElement(final JsonElement element, final String key) {
        if (element.isJsonPrimitive()) {
            final JsonPrimitive foundPrimitive = element.getAsJsonPrimitive();
            if (foundPrimitive.isString()) {
                return Arrays.asList(foundPrimitive.getAsString());
            }
        } else if (element.isJsonObject()) {
            final JsonObject jsonObject = element.getAsJsonObject();
            return (getValuesFromElement(jsonObject.get(key), key));
        } else if (element.isJsonArray()) {
            final JsonArray foundArray = element.getAsJsonArray();

            final List<String> foundValues = new ArrayList<>(foundArray.size());
            for (final JsonElement arrayElement : foundArray) {
                foundValues.addAll(getValuesFromElement(arrayElement, key));
            }
            return foundValues;
        }
        return Collections.emptyList();
    }
}
