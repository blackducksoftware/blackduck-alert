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
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class FieldAccessor extends AlertSerializableModel {
    private final Map<String, ConfigurationFieldModel> fields;

    public FieldAccessor(Map<String, ConfigurationFieldModel> fields) {
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

    public Boolean getBooleanOrFalse(String key) {
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
