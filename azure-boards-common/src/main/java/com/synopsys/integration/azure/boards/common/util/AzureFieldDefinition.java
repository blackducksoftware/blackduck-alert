/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.util;

import java.lang.reflect.Type;

import com.google.gson.JsonObject;

public class AzureFieldDefinition<T> {
    private final String fieldName;
    private final Type fieldType;

    public static AzureFieldDefinition<String> stringField(String fieldName) {
        return new AzureFieldDefinition<>(fieldName, String.class);
    }

    public static AzureFieldDefinition<Integer> integerField(String fieldName) {
        return new AzureFieldDefinition<>(fieldName, Integer.class);
    }

    public static AzureFieldDefinition<JsonObject> objectField(String fieldName) {
        return new AzureFieldDefinition<>(fieldName, JsonObject.class);
    }

    public AzureFieldDefinition(String fieldName, Type fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Type getFieldType() {
        return fieldType;
    }

}
