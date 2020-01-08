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
package com.synopsys.integration.alert.common.persistence.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ConfigurationFieldModel extends AlertSerializableModel {
    private final String fieldKey;
    private final Boolean isSensitive;
    private Collection<String> fieldValues;

    public static final ConfigurationFieldModel create(final String fieldKey) {
        return new ConfigurationFieldModel(fieldKey, Boolean.FALSE);
    }

    public static final ConfigurationFieldModel createSensitive(final String fieldKey) {
        return new ConfigurationFieldModel(fieldKey, Boolean.TRUE);
    }

    private ConfigurationFieldModel(final String fieldKey, final Boolean isSensitive) {
        this.fieldKey = fieldKey;
        this.isSensitive = isSensitive;
        fieldValues = null;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public boolean isSensitive() {
        return isSensitive.booleanValue();
    }

    public void setFieldValue(final String value) {
        if (StringUtils.isNotBlank(value)) {
            fieldValues = Collections.singleton(value);
        } else {
            fieldValues = null;
        }
    }

    public void setFieldValues(final Collection<String> values) {
        fieldValues = values;
    }

    public Optional<String> getFieldValue() {
        if (fieldValues != null) {
            return fieldValues.stream().findFirst();
        }
        return Optional.empty();
    }

    public Collection<String> getFieldValues() {
        if (fieldValues != null) {
            return Collections.unmodifiableCollection(fieldValues);
        }
        return Collections.emptySet();
    }

    public boolean isSet() {
        return fieldValues != null && !fieldValues.isEmpty();
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "fieldValues");
    }

    @Override
    public boolean equals(final Object that) {
        return EqualsBuilder.reflectionEquals(this, that, "fieldValues");
    }
}
