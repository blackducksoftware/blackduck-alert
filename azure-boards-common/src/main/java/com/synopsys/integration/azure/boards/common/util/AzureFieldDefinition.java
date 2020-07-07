/**
 * azure-boards-common
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
