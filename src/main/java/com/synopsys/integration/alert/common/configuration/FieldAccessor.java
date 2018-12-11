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
