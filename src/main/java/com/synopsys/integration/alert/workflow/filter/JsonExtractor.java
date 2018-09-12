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
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.common.field.StringHierarchicalField;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.web.model.Config;

@Component
public class JsonExtractor {
    private final Gson gson;

    @Autowired
    public JsonExtractor(final Gson gson) {
        this.gson = gson;
    }

    public List<String> getValuesFromConfig(final StringHierarchicalField field, final Config config) {
        final JsonElement element = gson.toJsonTree(config);
        final Optional<String> mapping = field.getConfigNameMapping();

        final List<String> values = new ArrayList<>();
        if (mapping.isPresent()) {
            final List<String> pathToField = Arrays.asList(mapping.get());
            final List<JsonElement> foundElements = getInnerElements(element, pathToField.listIterator());
            for (final JsonElement found : foundElements) {
                if (found.isJsonPrimitive()) {
                    values.add(found.getAsString());
                }
            }
        }
        return values;
    }

    // TODO This is a P.O.C.
    public List<LinkableItem> getLinkableItemsFromJson(final StringHierarchicalField dataField, final StringHierarchicalField linkField, final String json) {
        final List<String> values = getValuesFromJson(dataField, json);
        final List<String> links = getValuesFromJson(linkField, json);

        if (values.size() == links.size()) {
            final List<LinkableItem> linkableItems = new ArrayList<>();
            for (int i = 0; i < links.size(); i++) {
                linkableItems.add(new LinkableItem(dataField.getLabel(), values.get(i), links.get(i)));
            }
        }
        throw new IllegalArgumentException("The json provided did not contain the correct field pairings.");
    }

    public Optional<String> getFirstValueFromJson(final StringHierarchicalField hierarchicalField, final String json) {
        final List<String> foundValues = getValuesFromJson(hierarchicalField, json);
        if (!foundValues.isEmpty()) {
            return Optional.of(foundValues.get(0));
        }
        return Optional.empty();
    }

    public <T> Optional<T> getFirstValueFromJson(final HierarchicalField hierarchicalField, final String json) {
        final List<T> foundValues = getObjectsFromJson(hierarchicalField, json);
        if (!foundValues.isEmpty()) {
            return Optional.of(foundValues.get(0));
        }
        return Optional.empty();
    }

    public List<String> getValuesFromJson(final StringHierarchicalField field, final String json) {
        return getObjectsFromJson(field, json);
    }

    // TODO determine what we want to do about JsonSyntaxExceptions
    // TODO we know the type, so how do we get rid of generic types
    public <T> List<T> getObjectsFromJson(final HierarchicalField field, final String json) {
        final List<String> fieldNameHierarchy = field.getFullPathToField();
        final JsonObject object = gson.fromJson(json, JsonObject.class);

        final List<JsonElement> foundElements = getInnerElements(object, fieldNameHierarchy.listIterator());

        final List<T> objectsFromJson = new ArrayList<>();
        for (final JsonElement element : foundElements) {
            final T fromJson = gson.fromJson(element, field.getType());
            objectsFromJson.add(fromJson);
        }
        return objectsFromJson;
    }

    private List<JsonElement> getInnerElements(final JsonElement element, final ListIterator<String> path) {
        if (path.hasNext() && !element.isJsonPrimitive()) {
            final String nextKey = path.next();
            if (element.isJsonObject()) {
                final JsonObject jsonObject = element.getAsJsonObject();
                final JsonElement foundElement = jsonObject.get(nextKey);
                return getInnerElements(foundElement, path);
            } else if (element.isJsonArray()) {
                final JsonArray foundArray = element.getAsJsonArray();
                // TODO vulnerabilities with affected project version causes an issue.
                // iterating here the path iterator may be at the lowest level therefore there aren't
                // any additional items and we want to use the next key.  Need some work
                final List<JsonElement> foundValues = new ArrayList<>(foundArray.size());
                for (final JsonElement arrayElement : foundArray) {
                    foundValues.addAll(getInnerElements(arrayElement, path));
                }
                return foundValues;
            }
        }
        return Arrays.asList(element);
    }
}
