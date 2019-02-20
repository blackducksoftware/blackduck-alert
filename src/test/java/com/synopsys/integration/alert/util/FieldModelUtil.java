package com.synopsys.integration.alert.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.synopsys.integration.alert.common.data.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.data.model.FieldValueModel;

public class FieldModelUtil {

    public static void addConfigurationFieldToMap(final Map<String, ConfigurationFieldModel> map, final String key, final String value) {
        map.put(key, createConfigurationFieldModel(key, value));
    }

    public static void addConfigurationFieldToMap(final Map<String, ConfigurationFieldModel> map, final String key, final Collection<String> values) {
        map.put(key, createConfigurationFieldModel(key, values));
    }

    public static ConfigurationFieldModel createConfigurationFieldModel(final String key, final Collection<String> values) {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(key);
        field.setFieldValues(values);
        return field;
    }

    public static ConfigurationFieldModel createConfigurationFieldModel(final String key, final String value) {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(key);
        field.setFieldValue(value);
        return field;
    }

    public static void addFieldValueToMap(final Map<String, FieldValueModel> map, final String key, final String value) {
        map.put(key, createFieldValue(key, value));
    }

    public static void addFieldValueToMap(final Map<String, FieldValueModel> map, final String key, final Collection<String> values) {
        map.put(key, createFieldValue(key, values));
    }

    public static FieldValueModel createFieldValue(final String key, final Collection<String> values) {
        return new FieldValueModel(values, true);
    }

    public static FieldValueModel createFieldValue(final String key, final String value) {
        return new FieldValueModel(List.of(value), true);
    }

}
