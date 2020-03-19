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

public class FieldAccessor {
    private final Map<String, ConfigurationFieldModel> fields;

    public FieldAccessor(final Map<String, ConfigurationFieldModel> fields) {
        this.fields = fields;
    }

    public void addFields(final Map<String, ConfigurationFieldModel> newFields) {
        fields.putAll(newFields);
    }

    public Map<String, ConfigurationFieldModel> getFields() {
        return fields;
    }

    public Optional<ConfigurationFieldModel> getField(final String key) {
        return Optional.ofNullable(fields.get(key));
    }

    public Optional<Long> getLong(final String key) {
        final Optional<String> value = getValue(key);
        return value.map(Long::parseLong);
    }

    public Optional<Integer> getInteger(final String key) {
        final Optional<String> value = getValue(key);
        return value.map(Integer::parseInt);
    }

    public Optional<Boolean> getBoolean(final String key) {
        final Optional<String> value = getValue(key);
        return value.map(Boolean::parseBoolean);
    }

    public Optional<String> getString(final String key) {
        return getValue(key);
    }

    public Collection<String> getAllStrings(final String key) {
        if (fields.containsKey(key)) {
            return fields.get(key).getFieldValues();
        }
        return Set.of();
    }

    public <T extends Enum<T>> Optional<T> getEnum(final String key, final Class<T> enumClass) {
        final Optional<String> enumString = getString(key);
        return enumString.map(strValue -> EnumUtils.getEnum(enumClass, strValue));
    }

    private Optional<String> getValue(final String key) {
        if (StringUtils.isNotBlank(key) && fields.containsKey(key)) {
            final ConfigurationFieldModel fieldModel = fields.get(key);
            return fieldModel.getFieldValue();
        }
        return Optional.empty();
    }

    public boolean isSet(final String key) {
        if (StringUtils.isNotBlank(key) && fields.containsKey(key)) {
            final ConfigurationFieldModel fieldModel = fields.get(key);
            return fieldModel.isSet();
        }
        return false;
    }
}
