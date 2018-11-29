package com.synopsys.integration.alert.database.api.descriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.synopsys.integration.util.Stringable;

public class ConfigFieldModel extends Stringable {
    private final String fieldKey;
    private final Boolean isSensitive;
    private Collection<String> fieldValues;

    public static final ConfigFieldModel create(final String fieldKey) {
        return new ConfigFieldModel(fieldKey, Boolean.FALSE);
    }

    public static final ConfigFieldModel createSensitive(final String fieldKey) {
        return new ConfigFieldModel(fieldKey, Boolean.TRUE);
    }

    private ConfigFieldModel(final String fieldKey, final Boolean isSensitive) {
        this.fieldKey = fieldKey;
        this.isSensitive = isSensitive;
        this.fieldValues = null;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public boolean isSensitive() {
        return isSensitive.booleanValue();
    }

    public void setFieldValue(final String value) {
        this.fieldValues = Collections.singleton(value);
    }

    public void setFieldValues(final Collection<String> values) {
        this.fieldValues = values;
    }

    public Optional<String> getFieldValue() {
        if (fieldValues != null) {
            return fieldValues.stream().findFirst();
        }
        return Optional.empty();
    }

    public Collection<String> getFieldValues() {
        if (fieldValues != null) {
            return Collections.unmodifiableCollection(fieldValues);
        }
        return Collections.emptySet();
    }

    @Override
    public boolean equals(final Object that) {
        return EqualsBuilder.reflectionEquals(this, that, "fieldValues");
    }
}
