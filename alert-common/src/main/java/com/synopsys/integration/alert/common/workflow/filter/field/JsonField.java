/**
 * alert-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.common.workflow.filter.field;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;
import com.synopsys.integration.util.Stringable;

public class JsonField<T> extends Stringable {
    public static final String FORMAT_SINGLE_REPLACEMENT = "$.%s";
    public static final String FORMAT_DOUBLE_REPLACEMENT = "$.%s.%s";
    public static final String FORMAT_TRIPLE_REPLACEMENT = "$.%s.%s.%s";

    public static final String LABEL_URL_SUFFIX = "_url";

    private final TypeRef<List<T>> typeRef;
    private final FieldContentIdentifier contentIdentifier;
    private final List<JsonPath> configNameMappings;
    private final JsonPath jsonPath;

    private final String fieldName;
    private final String label;

    protected JsonField(final TypeRef<List<T>> typeRef, final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label) {
        this(typeRef, jsonPath, fieldName, contentIdentifier, label, null);
    }

    protected JsonField(final TypeRef<List<T>> typeRef, final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label, final List<JsonPath> configNameMappings) {
        this.fieldName = fieldName;
        this.label = label;
        this.typeRef = typeRef;
        this.jsonPath = jsonPath;
        this.contentIdentifier = contentIdentifier;
        this.configNameMappings = configNameMappings;
    }

    public static <NT> JsonField<NT> createObjectField(final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label, final TypeRef<List<NT>> typeRef) {
        return new JsonField<>(typeRef, jsonPath, fieldName, contentIdentifier, label);
    }

    public static JsonField<Long> createLongField(final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label) {
        return new JsonField<>(new TypeRef<>() {}, jsonPath, fieldName, contentIdentifier, label);
    }

    public static JsonField<Integer> createIntegerField(final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label) {
        return new JsonField<>(new TypeRef<>() {}, jsonPath, fieldName, contentIdentifier, label);
    }

    public static JsonField<String> createStringField(final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label) {
        return new JsonField<>(new TypeRef<>() {}, jsonPath, fieldName, contentIdentifier, label);
    }

    public static JsonField<String> createStringField(final JsonPath jsonPath, final String fieldName, final FieldContentIdentifier contentIdentifier, final String label, final List<JsonPath> configNameMappings) {
        return new JsonField<>(new TypeRef<>() {}, jsonPath, fieldName, contentIdentifier, label, configNameMappings);
    }

    public static JsonPath createJsonPath(final String pattern, final String... fields) {
        return JsonPath.compile(String.format(pattern, (Object[]) fields));
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

    public Optional<List<JsonPath>> getConfigNameMappings() {
        return Optional.ofNullable(configNameMappings);
    }

    public TypeRef<List<T>> getTypeRef() {
        return typeRef;
    }

    public boolean isOfType(final Type type) {
        return typeRef.getType().getTypeName().contains(type.getTypeName());
    }
}
