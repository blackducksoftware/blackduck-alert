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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;

public abstract class HierarchicalField extends Field {
    public static final String LABEL_URL_SUFFIX = "_url";

    private final boolean filterable;
    private final String configNameMapping;
    private List<String> fieldList;
    private final FieldContentIdentifier contentIdentifier;
    private final Type type;

    public HierarchicalField(final Collection<String> pathToField, final String innerMostFieldName, final FieldContentIdentifier contentIdentifier, final String label, final Type type) {
        super(innerMostFieldName, label);

        initFieldList(pathToField, innerMostFieldName);
        this.filterable = false;
        this.configNameMapping = null;
        this.contentIdentifier = contentIdentifier;
        this.type = type;
    }

    public HierarchicalField(final Collection<String> pathToField, final String innerMostFieldName, final FieldContentIdentifier contentIdentifier, final String label, final String configNameMapping, final Type type) {
        super(innerMostFieldName, label);

        initFieldList(pathToField, innerMostFieldName);
        this.filterable = true;
        this.configNameMapping = configNameMapping;
        this.contentIdentifier = contentIdentifier;
        this.type = type;
    }

    /**
     * @return an unmodifiable list of fields representing the path to a field nested within an object
     */
    public List<String> getFullPathToField() {
        return fieldList;
    }

    public Optional<String> getConfigNameMapping() {
        return Optional.ofNullable(configNameMapping);
    }

    public FieldContentIdentifier getContentIdentifier() {
        return contentIdentifier;
    }

    public Type getType() {
        return type;
    }

    public boolean isFilterable() {
        return filterable;
    }

    private void initFieldList(final Collection<String> pathToField, final String innerMostFieldName) {
        final List<String> list = new ArrayList<>();
        list.addAll(pathToField);
        list.add(innerMostFieldName);
        this.fieldList = Collections.unmodifiableList(list);
    }
}
