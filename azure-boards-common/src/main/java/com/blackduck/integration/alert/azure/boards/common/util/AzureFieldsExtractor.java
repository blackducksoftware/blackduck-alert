/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.util;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AzureFieldsExtractor {
    private final Gson gson;

    public AzureFieldsExtractor(Gson gson) {
        this.gson = gson;
    }

    public <T> Optional<T> extractField(JsonObject fieldsObject, AzureFieldDefinition<T> fieldDefinition) {
        JsonElement foundField = fieldsObject.get(fieldDefinition.getFieldName());
        if (null != foundField) {
            T fieldValue = gson.fromJson(foundField, fieldDefinition.getFieldType());
            return Optional.of(fieldValue);
        }
        return Optional.empty();
    }

}
