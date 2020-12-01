/**
 * test-common
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
package com.synopsys.integration.alert.test.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class FieldModelUtils {
    public static void addConfigurationFieldToMap(Map<String, ConfigurationFieldModel> map, String key, String value) {
        map.put(key, createConfigurationFieldModel(key, value));
    }

    public static void addConfigurationFieldToMap(Map<String, ConfigurationFieldModel> map, String key, Collection<String> values) {
        map.put(key, createConfigurationFieldModel(key, values));
    }

    public static ConfigurationFieldModel createConfigurationFieldModel(String key, Collection<String> values) {
        ConfigurationFieldModel field = ConfigurationFieldModel.create(key);
        field.setFieldValues(values);
        return field;
    }

    public static ConfigurationFieldModel createConfigurationFieldModel(String key, String value) {
        ConfigurationFieldModel field = ConfigurationFieldModel.create(key);
        field.setFieldValue(value);
        return field;
    }

    public static void addFieldValueToMap(Map<String, FieldValueModel> map, String key, String value) {
        map.put(key, createFieldValue(key, value));
    }

    public static void addFieldValueToMap(Map<String, FieldValueModel> map, String key, Collection<String> values) {
        map.put(key, createFieldValue(key, values));
    }

    public static FieldValueModel createFieldValue(String key, Collection<String> values) {
        return new FieldValueModel(values, true);
    }

    public static FieldValueModel createFieldValue(String key, String value) {
        return new FieldValueModel(List.of(value), true);
    }

}
