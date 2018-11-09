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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;

public class HierarchicalField<T> extends Field {
    public static final String LABEL_URL_SUFFIX = "_url";

    private final Class<T> type;
    private final FieldContentIdentifier contentIdentifier;
    private final String configNameMapping;
    private final List<String> fieldPath;

    public static <T> HierarchicalField<T> createObjectField(final List<String> fieldPath, final FieldContentIdentifier contentIdentifier, final String label, final Class<T> fieldClazz) {
        return new HierarchicalField<>(fieldClazz, fieldPath, getInnerMostFieldName(fieldPath), contentIdentifier, label);
    }

    public static HierarchicalField<Long> createLongField(final List<String> fieldPath, final FieldContentIdentifier contentIdentifier, final String label) {
        return new HierarchicalField<>(Long.class, fieldPath, getInnerMostFieldName(fieldPath), contentIdentifier, label);
    }

    public static HierarchicalField<String> createStringField(final List<String> fieldPath, final FieldContentIdentifier contentIdentifier, final String label) {
        return new HierarchicalField<>(String.class, fieldPath, getInnerMostFieldName(fieldPath), contentIdentifier, label);
    }

    public static HierarchicalField<String> createStringField(final List<String> fieldPath, final FieldContentIdentifier contentIdentifier, final String label, final String configNameMapping) {
        return new HierarchicalField<>(String.class, fieldPath, getInnerMostFieldName(fieldPath), contentIdentifier, label, configNameMapping);
    }

    private static String getInnerMostFieldName(final List<String> fieldPath) {
        if (fieldPath != null && !fieldPath.isEmpty()) {
            return fieldPath.get(fieldPath.size() - 1);
        }
        return null;
    }

    protected HierarchicalField(final Class<T> clazz, final List<String> fullFieldPath, final String innerMostFieldName, final FieldContentIdentifier contentIdentifier, final String label) {
        this(clazz, fullFieldPath, innerMostFieldName, contentIdentifier, label, null);
    }

    protected HierarchicalField(final Class<T> clazz, final List<String> fullFieldPath, final String innerMostFieldName, final FieldContentIdentifier contentIdentifier, final String label, final String configNameMapping) {
        super(innerMostFieldName, label);
        this.type = clazz;
        this.fieldPath = fullFieldPath;
        this.contentIdentifier = contentIdentifier;
        this.configNameMapping = configNameMapping;
    }

    /**
     * @return an unmodifiable list of fields representing the path to a field nested within an object
     */
    public List<String> getPathToField() {
        return Collections.unmodifiableList(fieldPath);
    }

    public FieldContentIdentifier getContentIdentifier() {
        return contentIdentifier;
    }

    public Optional<String> getConfigNameMapping() {
        return Optional.ofNullable(configNameMapping);
    }

    public Type getType() {
        return type;
    }
}
