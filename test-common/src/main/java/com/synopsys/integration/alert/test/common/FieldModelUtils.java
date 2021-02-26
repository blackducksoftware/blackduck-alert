/*
 * test-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
