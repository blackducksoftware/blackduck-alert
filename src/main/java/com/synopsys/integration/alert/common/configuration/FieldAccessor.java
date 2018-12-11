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

import org.apache.commons.lang3.EnumUtils;

import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;

public class FieldAccessor {
    private final Map<String, ConfigurationFieldModel> fields;

    public FieldAccessor(final Map<String, ConfigurationFieldModel> fields) {
        this.fields = fields;
    }

    public Long getLong(final String key) {
        final String value = fields.get(key).getFieldValue().orElse("");
        return Long.parseLong(value);
    }

    public Integer getInteger(final String key) {
        final String value = fields.get(key).getFieldValue().orElse("");
        return Integer.parseInt(value);
    }

    public Boolean getBoolean(final String key) {
        final String value = fields.get(key).getFieldValue().orElse(Boolean.FALSE.toString());
        return Boolean.parseBoolean(value);
    }

    public String getString(final String key) {
        return fields.get(key).getFieldValue().orElse("");
    }

    public Collection<String> getAllStrings(final String key) {
        return fields.get(key).getFieldValues();
    }

    public <T extends Enum<T>> T getEnum(final String key, final Class<T> enumClass) {
        final String enumString = getString(key);
        return EnumUtils.getEnum(enumClass, enumString);
    }

}
