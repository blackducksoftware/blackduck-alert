package com.synopsys.integration.alert.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.web.model.FieldModel;

@Component
public class ConfigurationFieldModelConverter {
    private final DescriptorMap descriptorMap;

    @Autowired
    public ConfigurationFieldModelConverter(final DescriptorMap descriptorMap) {
        this.descriptorMap = descriptorMap;
    }

    public Map<String, ConfigurationFieldModel> convertFromFieldModel(final FieldModel fieldModel) {
        final Map<String, ConfigField> configFieldMap = retrieveUIConfigFields(fieldModel).stream().collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        if (configFieldMap.isEmpty()) {
            return Map.of();
        }

        return convertToConfigurationFieldModelMap(configFieldMap, fieldModel);
    }

    private List<ConfigField> retrieveUIConfigFields(final FieldModel fieldModel) {
        final String context = fieldModel.getContext();
        final ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
        return descriptorMap.getDescriptor(fieldModel.getDescriptorName())
                   .map(descriptor -> descriptor.getUIConfig(descriptorContext))
                   .flatMap(config -> config)
                   .map(config -> config.createFields())
                   .orElse(List.of());
    }

    private Map<String, ConfigurationFieldModel> convertToConfigurationFieldModelMap(final Map<String, ConfigField> configFieldMap, final FieldModel fieldModel) {
        return fieldModel.getKeyToValues()
                   .entrySet()
                   .stream()
                   .filter(entry -> configFieldMap.containsKey(entry.getKey()))
                   .collect(Collectors.toMap(Map.Entry::getKey, entry -> createConfigurationFieldModel(configFieldMap.get(entry.getKey()), entry.getValue().getValues())));
    }

    private ConfigurationFieldModel createConfigurationFieldModel(final ConfigField configField, final Collection<String> values) {
        final ConfigurationFieldModel configurationFieldModel;
        final String key = configField.getKey();
        if (configField.isSensitive()) {
            configurationFieldModel = ConfigurationFieldModel.createSensitive(key);
        } else {
            configurationFieldModel = ConfigurationFieldModel.create(key);
        }
        configurationFieldModel.setFieldValues(values);
        return configurationFieldModel;
    }
}
