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
package com.synopsys.integration.alert.common.field;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;
import com.synopsys.integration.util.Stringable;

public class JsonField<T> extends Stringable {
    public static final String LABEL_URL_SUFFIX = "_url";

    private final TypeRef<?> typeRef;
    private final FieldContentIdentifier contentIdentifier;
    private final JsonPath configNameMapping;
    private final JsonPath jsonPath;

    private final String fieldName;
    private final String label;

    public static <T> JsonField<T> createObjectField(final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label, final TypeRef<?> typeRef) {
        return new JsonField<T>(typeRef, jsonPath, fieldName, contentIdentifier, label);
    }

    public static JsonField<Long> createLongField(final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label) {
        return new JsonField<Long>(new TypeRef<List<Long>>() {}, jsonPath, fieldName, contentIdentifier, label);
    }

    public static JsonField<String> createStringField(final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label) {
        return new JsonField<String>(new TypeRef<List<String>>() {}, jsonPath, fieldName, contentIdentifier, label);
    }

    public static JsonField<String> createStringField(final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label, final JsonPath configNameMapping) {
        return new JsonField<String>(new TypeRef<List<String>>() {}, jsonPath, fieldName, contentIdentifier, label, configNameMapping);
    }

    protected JsonField(final TypeRef<?> typeRef, final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label) {
        this(typeRef, jsonPath, fieldName, contentIdentifier, label, null);
    }

    protected JsonField(final TypeRef<?> typeRef, final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label, final JsonPath configNameMapping) {
        this.fieldName = fieldName;
        this.label = label;
        this.typeRef = typeRef;
        this.jsonPath = jsonPath;
        this.contentIdentifier = contentIdentifier;
        this.configNameMapping = configNameMapping;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getLabel() {
        return label;
    }

    public JsonPath getJsonPath() {
        return jsonPath;
    }

    public FieldContentIdentifier getContentIdentifier() {
        return contentIdentifier;
    }

    public Optional<JsonPath> getConfigNameMapping() {
        return Optional.ofNullable(configNameMapping);
    }

    public TypeRef<?> getTypeRef() {
        return typeRef;
    }

    public boolean isOfType(final Type type) {
        return typeRef.getType().getTypeName().contains(type.getTypeName());
    }
}
