/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;

public class FieldUtility extends AlertSerializableModel {
    private final Map<String, ConfigurationFieldModel> fields;

    public FieldUtility(Map<String, ConfigurationFieldModel> fields) {
        this.fields = fields;
    }

    public void addFields(Map<String, ConfigurationFieldModel> newFields) {
        fields.putAll(newFields);
    }

    public Map<String, ConfigurationFieldModel> getFields() {
        return fields;
    }

    public Optional<ConfigurationFieldModel> getField(String key) {
        return Optional.ofNullable(fields.get(key));
    }

    public Optional<Long> getLong(String key) {
        Optional<String> value = getValue(key);
        return value.map(Long::parseLong);
    }

    public Optional<Integer> getInteger(String key) {
        Optional<String> value = getValue(key);
        return value.map(Integer::parseInt);
    }

    public Optional<Boolean> getBoolean(String key) {
        Optional<String> value = getValue(key);
        return value.map(Boolean::parseBoolean);
    }

    public boolean getBooleanOrFalse(String key) {
        return getBoolean(key).orElse(Boolean.FALSE);
    }

    public Optional<String> getString(String key) {
        return getValue(key);
    }

    public String getStringOrNull(String key) {
        return getValue(key).orElse(null);
    }

    public String getStringOrEmpty(String key) {
        return getValue(key).orElse(StringUtils.EMPTY);
    }

    public Collection<String> getAllStrings(String key) {
        if (fields.containsKey(key)) {
            return fields.get(key).getFieldValues();
        }
        return Set.of();
    }

    public <T extends Enum<T>> Optional<T> getEnum(String key, Class<T> enumClass) {
        Optional<String> enumString = getString(key);
        return enumString.map(strValue -> EnumUtils.getEnum(enumClass, strValue));
    }

    private Optional<String> getValue(String key) {
        if (StringUtils.isNotBlank(key) && fields.containsKey(key)) {
            ConfigurationFieldModel fieldModel = fields.get(key);
            return fieldModel.getFieldValue();
        }
        return Optional.empty();
    }

    public boolean isSet(String key) {
        if (StringUtils.isNotBlank(key) && fields.containsKey(key)) {
            ConfigurationFieldModel fieldModel = fields.get(key);
            return fieldModel.isSet();
        }
        return false;
    }

}
