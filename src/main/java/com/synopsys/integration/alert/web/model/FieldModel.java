package com.synopsys.integration.alert.web.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.database.api.descriptor.ConfigurationFieldModel;

public class FieldModel extends Config {
    private final Map<String, Collection<String>> keyToValues;

    public FieldModel(final Map<String, Collection<String>> keyToValues) {
        this.keyToValues = keyToValues;
    }

    public Map<String, Collection<String>> getKeyToValues() {
        return keyToValues;
    }

    public Optional<String> getValue(final String key) {
        return keyToValues.get(key).stream().findFirst();
    }

    public Collection<String> getValues(final String key) {
        return keyToValues.get(key);
    }

    public void putString(final String key, final String value) {
        putStrings(key, Arrays.asList(value));
    }

    public void putStrings(final String key, final Collection<String> values) {
        keyToValues.put(key, values);
    }

    public FieldAccessor convertToFieldAccessor() {
        final Map<String, ConfigurationFieldModel> fields = keyToValues
                                                                .entrySet()
                                                                .stream()
                                                                .collect(Collectors.toMap(Map.Entry::getKey, entry -> createConfigurationFieldModel(entry.getKey(), entry.getValue())));
        return new FieldAccessor(fields);
    }

    private ConfigurationFieldModel createConfigurationFieldModel(final String key, final Collection<String> values) {
        final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValues(values);
        return configurationFieldModel;
    }

}
