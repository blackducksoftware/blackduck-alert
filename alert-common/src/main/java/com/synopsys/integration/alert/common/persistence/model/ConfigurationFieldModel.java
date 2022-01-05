/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class ConfigurationFieldModel extends AlertSerializableModel {
    private final String fieldKey;
    private final Boolean isSensitive;
    private Collection<String> fieldValues;

    public static final ConfigurationFieldModel create(String fieldKey) {
        return new ConfigurationFieldModel(fieldKey, Boolean.FALSE);
    }

    public static final ConfigurationFieldModel createSensitive(String fieldKey) {
        return new ConfigurationFieldModel(fieldKey, Boolean.TRUE);
    }

    private ConfigurationFieldModel(String fieldKey, Boolean isSensitive) {
        this.fieldKey = fieldKey;
        this.isSensitive = isSensitive;
        fieldValues = null;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public boolean isSensitive() {
        return BooleanUtils.isTrue(isSensitive);
    }

    public void setFieldValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            fieldValues = Collections.singleton(value);
        } else {
            fieldValues = null;
        }
    }

    public void setFieldValues(Collection<String> values) {
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
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that, "fieldValues");
    }

}
