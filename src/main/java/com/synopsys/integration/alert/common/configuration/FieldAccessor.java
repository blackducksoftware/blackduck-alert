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
package com.synopsys.integration.alert.common.configuration;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;

import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;

public class FieldAccessor {
    private final Map<String, ConfigurationFieldModel> fields;

    public FieldAccessor(final Map<String, ConfigurationFieldModel> fields) {
        this.fields = fields;
    }

    public Optional<Long> getLong(final String key) {
        final Optional<String> value = getValue(key);
        if (!value.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(value.get()));
    }

    public Optional<Integer> getInteger(final String key) {
        final Optional<String> value = getValue(key);
        if (!value.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(value.get()));
    }

    public Optional<Boolean> getBoolean(final String key) {
        final Optional<String> value = getValue(key);
        if (!value.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(Boolean.parseBoolean(value.get()));
    }

    public Optional<String> getString(final String key) {
        return getValue(key);
    }

    public Collection<String> getAllStrings(final String key) {
        return fields.get(key).getFieldValues();
    }

    public <T extends Enum<T>> Optional<T> getEnum(final String key, final Class<T> enumClass) {
        final Optional<String> enumString = getString(key);
        if (!enumString.isPresent()) {
            return Optional.empty();
        }
        return Optional.ofNullable(EnumUtils.getEnum(enumClass, enumString.get()));
    }

    private Optional<String> getValue(final String key) {
        final ConfigurationFieldModel fieldModel = fields.get(key);
        if (fieldModel == null) {
            return Optional.empty();
        }

        return fieldModel.getFieldValue();
    }

}
