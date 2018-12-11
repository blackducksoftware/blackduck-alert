package com.synopsys.integration.alert.web.model;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;

public class FieldModel extends Config {
    private final Map<String, FieldValueModel> keyToValues;
    private final String descriptorName;
    private final String context;

    public FieldModel(final String configId, final String descriptorName, final String context, final Map<String, FieldValueModel> keyToValues) {
        super(configId);
        this.descriptorName = descriptorName;
        this.context = context;
        this.keyToValues = keyToValues;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

    public String getContext() {
        return context;
    }

    public Map<String, FieldValueModel> getKeyToValues() {
        return keyToValues;
    }

    public FieldValueModel getField(String key) {
        return keyToValues.get(key);
    }

    public FieldAccessor convertToFieldAccessor() {
        final Map<String, ConfigurationFieldModel> fields = convertToConfigurationFieldModelMap();
        return new FieldAccessor(fields);
    }

    public Map<String, ConfigurationFieldModel> convertToConfigurationFieldModelMap() {
        return keyToValues
                   .entrySet()
                   .stream()
                   .collect(Collectors.toMap(Map.Entry::getKey, entry -> createConfigurationFieldModel(entry.getKey(), entry.getValue().getValues())));
    }

    private ConfigurationFieldModel createConfigurationFieldModel(final String key, final Collection<String> values) {
        final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValues(values);
        return configurationFieldModel;
    }

}
