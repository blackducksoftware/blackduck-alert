/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest;

import java.util.List;

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

    public ResponseBodyBuilder putErrors(List<AlertFieldStatus> errors) {
        JsonObject element = new JsonObject();
        for (AlertFieldStatus alertFieldStatus : errors) {
            JsonObject statusObject = new JsonObject();
            statusObject.addProperty(PROPERTY_KEY_SEVERITY, alertFieldStatus.getSeverity().name());
            statusObject.addProperty(PROPERTY_KEY_FIELD_MESSAGE, alertFieldStatus.getFieldMessage());
            element.add(alertFieldStatus.getFieldName(), statusObject);
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
