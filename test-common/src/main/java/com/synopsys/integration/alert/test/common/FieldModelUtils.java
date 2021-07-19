/*
 * test-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;

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
        map.put(key, createFieldValue(value));
    }

    public static void addFieldValueToMap(Map<String, FieldValueModel> map, String key, Collection<String> values) {
        map.put(key, createFieldValue(values));
    }

    public static FieldValueModel createFieldValue(Collection<String> values) {
        return new FieldValueModel(values, true);
    }

    public static FieldValueModel createFieldValue(String value) {
        return new FieldValueModel(List.of(value), true);
    }

    public static <K extends String, V extends FieldValueModel> JobFieldModel createJobFieldModel(K k1, V v1) {
        return createJobFieldModel(Map.of(k1, v1));
    }

    public static JobFieldModel createJobFieldModel(Map<String, FieldValueModel>... multipleMaps) {
        Map<String, FieldValueModel> finalMap = Arrays.stream(multipleMaps).collect(HashMap::new, Map::putAll, Map::putAll);
        FieldModel fieldModel = new FieldModel("someDescriptor", "someContext", finalMap);
        return new JobFieldModel("jobId", Set.of(fieldModel), List.of());
    }

    public static JobFieldModel createJobFieldModel(Map<String, FieldValueModel> fieldValueModelMap) {
        FieldModel fieldModel = new FieldModel("someDescriptor", "someContext", fieldValueModelMap);
        return new JobFieldModel("jobId", Set.of(fieldModel), List.of());
    }
}
