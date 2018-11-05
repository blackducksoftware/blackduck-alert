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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.util.Stringable;

@Component
public class JsonExtractor {
    private final Gson gson;

    @Autowired
    public JsonExtractor(final Gson gson) {
        this.gson = gson;
    }

    public JsonFieldAccessor createJsonFieldAccessor(final List<HierarchicalField<?>> fields, final String json) {
        final Map<HierarchicalField, List<Object>> fieldToValuesMap = new HashMap<>();
        final JsonObject jsonObjectModel = gson.fromJson(json, JsonObject.class);

        for (final HierarchicalField field : fields) {
            final List<String> fieldNameHierarchy = field.getPathToField();
            final Type fieldDataType = field.getType();

            final List<JsonElement> foundElements = getInnerElements(jsonObjectModel, fieldNameHierarchy);
            final List<Object> values = getValuesFromJsonElements(foundElements, jsonElement -> gson.fromJson(jsonElement, fieldDataType));

            fieldToValuesMap.put(field, values);
        }
        return new JsonFieldAccessor(fieldToValuesMap);
    }

    public List<String> getValuesFromConfig(final HierarchicalField<String> field, final Config config) {
        final JsonElement jsonConfigModel = gson.toJsonTree(config);
        final Optional<String> mapping = field.getConfigNameMapping();

        final List<String> values = new ArrayList<>();
        if (mapping.isPresent()) {
            final List<String> pathToField = Arrays.asList(mapping.get());
            final List<JsonElement> foundElements = getInnerElements(jsonConfigModel, pathToField);
            for (final JsonElement found : foundElements) {
                if (found.isJsonPrimitive()) {
                    values.add(found.getAsString());
                } else if (found.isJsonArray()) {
                    final List<String> valuesFromArray = getValuesFromJsonElements(found.getAsJsonArray(), jsonElement -> jsonElement.getAsString());
                    values.addAll(valuesFromArray);
                }
            }
        }
        return values;
    }

    public <T> List<T> getValuesFromJson(final HierarchicalField<T> field, final String json) {
        final List<String> fieldNameHierarchy = field.getPathToField();
        final JsonObject object = gson.fromJson(json, JsonObject.class);

        final List<JsonElement> foundElements = getInnerElements(object, fieldNameHierarchy);
        return getValuesFromJsonElements(foundElements, jsonElement -> gson.fromJson(jsonElement, field.getType()));
    }

    private List<JsonElement> getInnerElements(final JsonElement element, final List<String> path) {
        return getInnerElements(element, buildPathLinkedList(path));
    }

    private List<JsonElement> getInnerElements(final JsonElement element, final PathNode pathNode) {
        if (element == null) {
            return Collections.emptyList();
        } else {
            if (element.isJsonPrimitive()) {
                return Arrays.asList(element);
            } else {
                if (pathNode != null && element.isJsonObject()) {
                    final String key = pathNode.getKey();
                    final JsonObject jsonObject = element.getAsJsonObject();
                    final JsonElement foundElement = jsonObject.get(key);
                    return getInnerElements(foundElement, pathNode.getNextNode());
                } else if (element.isJsonArray()) {
                    final JsonArray foundArray = element.getAsJsonArray();
                    final List<JsonElement> foundValues = new ArrayList<>(foundArray.size());
                    for (final JsonElement arrayElement : foundArray) {
                        foundValues.addAll(getInnerElements(arrayElement, pathNode));
                    }
                    return foundValues;
                }
                return Arrays.asList(element);
            }
        }
    }

    private <T> List<T> getValuesFromJsonElements(final Iterable<JsonElement> elementList, final Function<JsonElement, T> function) {
        final List<T> objectsFromJson = new ArrayList<>();
        for (final JsonElement element : elementList) {
            objectsFromJson.add(function.apply(element));
        }
        return objectsFromJson;
    }

    private PathNode buildPathLinkedList(final List<String> fullPathToField) {
        PathNode previousNode = null;
        PathNode firstNode = null;

        for (final String key : fullPathToField) {
            final PathNode currentNode = new PathNode(key);
            if (firstNode == null) {
                firstNode = currentNode;
            }
            if (previousNode != null) {
                previousNode.setNextNode(currentNode);
            }
            previousNode = currentNode;
        }
        return firstNode;
    }

    private class PathNode extends Stringable {
        private final String key;
        private PathNode nextNode;

        public PathNode(final String key) {
            this.key = key;
            this.nextNode = null;
        }

        public String getKey() {
            return key;
        }

        public PathNode getNextNode() {
            return nextNode;
        }

        public void setNextNode(final PathNode nextNode) {
            this.nextNode = nextNode;
        }
    }
}
