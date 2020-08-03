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
package com.synopsys.integration.alert.common.rest;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;

public class ResponseBodyBuilder {
    private static final String PROPERTY_KEY_ID = "id";
    private static final String PROPERTY_KEY_MESSAGE = "message";
    private static final String PROPERTY_KEY_SEVERITY = "severity";
    private static final String PROPERTY_KEY_FIELD_MESSAGE = "fieldMessage";
    private static final String PROPERTY_KEY_ERRORS = "errors";

    private final JsonObject map;

    public ResponseBodyBuilder() {
        map = new JsonObject();
    }

    public ResponseBodyBuilder(String id, String message) {
        this();
        if (!ResponseFactory.EMPTY_ID.equals(id)) {
            map.addProperty(PROPERTY_KEY_ID, id);
        }
        map.addProperty(PROPERTY_KEY_MESSAGE, message);
    }

    public ResponseBodyBuilder(String message) {
        this(ResponseFactory.EMPTY_ID, message);
    }

    public ResponseBodyBuilder put(String key, Boolean value) {
        map.addProperty(key, value);
        return this;
    }

    public ResponseBodyBuilder put(String key, Number value) {
        map.addProperty(key, value);
        return this;
    }

    public ResponseBodyBuilder put(String key, String value) {
        map.addProperty(key, value);
        return this;
    }

    public ResponseBodyBuilder putErrors(Map<String, AlertFieldStatus> errors) {
        JsonObject element = new JsonObject();
        for (Entry<String, AlertFieldStatus> entry : errors.entrySet()) {
            AlertFieldStatus alertFieldStatus = entry.getValue();
            JsonObject statusObject = new JsonObject();
            statusObject.addProperty(PROPERTY_KEY_SEVERITY, alertFieldStatus.getSeverity().name());
            statusObject.addProperty(PROPERTY_KEY_FIELD_MESSAGE, alertFieldStatus.getFieldMessage());
            element.add(entry.getKey(), statusObject);
        }
        map.add(PROPERTY_KEY_ERRORS, element);
        return this;
    }

    public String build() {
        return toString();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
