/*
 * test-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
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
    public static final String DEFAULT_MODEL_DESCRIPTOR_NAME = "Descriptor Name";
    public static final String DEFAULT_MODEL_CONTEXT = "context";
    public static final String DEFAULT_MODEL_JOB_ID = "id";

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

    public static FieldValueModel createFieldValue(String value) {
        return new FieldValueModel(List.of(value), true);
    }

    @SafeVarargs
    public static JobFieldModel createJobFieldModel(Map<String, FieldValueModel>... multipleMaps) {
        Map<String, FieldValueModel> finalMap = Arrays.stream(multipleMaps).collect(HashMap::new, Map::putAll, Map::putAll);
        return createJobFieldModel(finalMap);
    }

    public static JobFieldModel createJobFieldModel(Map<String, FieldValueModel> fieldValueModelMap) {
        FieldModel fieldModel = new FieldModel(DEFAULT_MODEL_DESCRIPTOR_NAME, DEFAULT_MODEL_CONTEXT, fieldValueModelMap);
        return new JobFieldModel(DEFAULT_MODEL_JOB_ID, Set.of(fieldModel), List.of());
    }

}
