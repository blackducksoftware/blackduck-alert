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

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.database.entity.NotificationContent;

public class JsonFieldFilter implements JsonFilterBuilder {
    private final Gson gson;
    private final List<String> fieldNameHierarchy;
    private final String value;

    public JsonFieldFilter(final Gson gson, final String fieldName, final String value) {
        this(gson, Arrays.asList(fieldName), value);
    }

    public JsonFieldFilter(final Gson gson, final List<String> fieldNameHierarchy, final String value) {
        this.gson = gson;
        this.fieldNameHierarchy = fieldNameHierarchy;
        this.value = value;
    }

    @Override
    public Predicate<NotificationContent> buildPredicate() {
        final Predicate<NotificationContent> contentPredicate = (notification) -> {
            JsonObject object = gson.fromJson(notification.getContent(), JsonObject.class);
            JsonElement foundObject = object;
            // traverse the json tree a node at a time.
            for (final String fieldName : fieldNameHierarchy) {
                foundObject = object.get(fieldName);
                if (foundObject != null) {
                    if (foundObject.isJsonObject()) {
                        object = foundObject.getAsJsonObject();
                    }
                } else {
                    // stop at the first failure in the tree hierarchy.
                    break;
                }
            }

            if (foundObject != null) {
                return value.equals(foundObject.getAsString());
            } else {
                return false;
            }
        };

        return contentPredicate;
    }
}
