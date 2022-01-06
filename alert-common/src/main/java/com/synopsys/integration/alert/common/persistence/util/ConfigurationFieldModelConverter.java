/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class ConfigurationFieldModelConverter {
    private final EncryptionUtility encryptionUtility;
    private final DescriptorAccessor descriptorAccessor;
    private final Map<String, DescriptorKey> descriptorKeys;

    private static final String MISSING_REGISTERED_DESCRIPTOR_MESSAGE = "Expected to find registered descriptor but none was found.";

    @Autowired
    public ConfigurationFieldModelConverter(EncryptionUtility encryptionUtility, DescriptorAccessor descriptorAccessor, List<DescriptorKey> descriptorKeys) {
        this.encryptionUtility = encryptionUtility;
        this.descriptorAccessor = descriptorAccessor;
        this.descriptorKeys = DataStructureUtils.mapToValues(descriptorKeys, DescriptorKey::getUniversalKey);
    }

    public final FieldUtility convertToFieldAccessor(FieldModel fieldModel) {
        Map<String, ConfigurationFieldModel> fields = convertToConfigurationFieldModelMap(fieldModel);
        return new FieldUtility(fields);
    }

    public final Optional<ConfigurationFieldModel> convertFromDefinedFieldModel(DefinedFieldModel definedFieldModel, String value, boolean isSet) {
        boolean actualSetValue = isSet || StringUtils.isNotBlank(value);
        if (!actualSetValue) {
            return Optional.empty();
        }

        Optional<ConfigurationFieldModel> configurationModel = createEmptyModel(definedFieldModel);
        configurationModel.ifPresent(model -> model.setFieldValue(value));
        return configurationModel;
    }

    public final Optional<ConfigurationFieldModel> convertFromDefinedFieldModel(DefinedFieldModel definedFieldModel, Collection<String> values, boolean isSet) {
        boolean actualSetValue = isSet || (values != null && values.stream().anyMatch(StringUtils::isNotBlank));
        if (!actualSetValue) {
            return Optional.empty();
        }

        Optional<ConfigurationFieldModel> configurationModel = createEmptyModel(definedFieldModel);
        configurationModel.ifPresent(model -> model.setFieldValues(values));
        return configurationModel;
    }

    public final Map<String, ConfigurationFieldModel> convertToConfigurationFieldModelMap(FieldModel fieldModel) {
        ConfigContextEnum context = EnumUtils.getEnum(ConfigContextEnum.class, fieldModel.getContext());
        String descriptorName = fieldModel.getDescriptorName();
        DescriptorKey descriptorKey = getDescriptorKey(descriptorName).orElseThrow(() -> new AlertRuntimeException("Could not find a Descriptor with the name: " + descriptorName));

        List<DefinedFieldModel> fieldsForContext = descriptorAccessor.getFieldsForDescriptor(descriptorKey, context);
        Map<String, ConfigurationFieldModel> configurationModels = new HashMap<>();
        for (DefinedFieldModel definedField : fieldsForContext) {
            fieldModel.getFieldValueModel(definedField.getKey())
                .flatMap(fieldValueModel -> convertFromDefinedFieldModel(definedField, fieldValueModel.getValues(), fieldValueModel.getIsSet()))
                .ifPresent(configurationFieldModel -> configurationModels.put(configurationFieldModel.getFieldKey(), configurationFieldModel));
        }

        return configurationModels;
    }

    public Map<String, FieldValueModel> convertToFieldValuesMap(Collection<ConfigurationFieldModel> configurationFieldModels) {
        Map<String, FieldValueModel> fields = new HashMap<>();
        for (ConfigurationFieldModel fieldModel : configurationFieldModels) {
            String key = fieldModel.getFieldKey();
            Collection<String> values = fieldModel.getFieldValues();
            FieldValueModel fieldValueModel = new FieldValueModel(values, fieldModel.isSet());
            fields.put(key, fieldValueModel);
        }
        return fields;
    }

    public FieldModel convertToFieldModel(ConfigurationModel configurationModel) {
        Long configId = configurationModel.getConfigurationId();
        String descriptorName = getDescriptorName(configurationModel);
        Map<String, FieldValueModel> fields = new HashMap<>();
        for (ConfigurationFieldModel fieldModel : configurationModel.getCopyOfFieldList()) {
            populateAndSecureFields(fieldModel, fields);
        }

        return new FieldModel(configId.toString(), descriptorName, configurationModel.getDescriptorContext().name(),
            configurationModel.getCreatedAt(), configurationModel.getLastUpdated(), fields);
    }

    public ConfigurationModel convertToConfigurationModel(FieldModel fieldModel) {
        String descriptorName = fieldModel.getDescriptorName();
        DescriptorKey descriptorKey = getDescriptorKey(descriptorName).orElseThrow(() -> new AlertRuntimeException("Could not find a Descriptor with the name: " + descriptorName));

        long descriptorId = descriptorAccessor.getRegisteredDescriptorByKey(descriptorKey).map(RegisteredDescriptorModel::getId).orElse(0L);
        long configId = Long.parseLong(fieldModel.getId());
        ConfigurationModelMutable configurationModel = new ConfigurationModelMutable(configId, descriptorId, fieldModel.getCreatedAt(), fieldModel.getLastUpdated(), fieldModel.getContext());
        convertToConfigurationFieldModelMap(fieldModel).values().forEach(configurationModel::put);
        return configurationModel;
    }

    private Optional<DescriptorKey> getDescriptorKey(String key) {
        return Optional.ofNullable(descriptorKeys.get(key));
    }

    private Optional<ConfigurationFieldModel> createEmptyModel(DefinedFieldModel definedFieldModel) {
        ConfigurationFieldModel configurationModel = ConfigurationFieldModel.create(definedFieldModel.getKey());
        if (BooleanUtils.isTrue(definedFieldModel.getSensitive())) {
            if (!encryptionUtility.isInitialized()) {
                return Optional.empty();
            }
            configurationModel = ConfigurationFieldModel.createSensitive(definedFieldModel.getKey());
        }
        return Optional.of(configurationModel);
    }

    private void populateAndSecureFields(ConfigurationFieldModel fieldModel, Map<String, FieldValueModel> fields) {
        String key = fieldModel.getFieldKey();
        Collection<String> values = (!fieldModel.isSensitive()) ? fieldModel.getFieldValues() : List.of();
        FieldValueModel fieldValueModel = new FieldValueModel(values, fieldModel.isSet());
        fields.put(key, fieldValueModel);
    }

    private String getDescriptorName(ConfigurationModel configurationModel) {
        return descriptorAccessor.getRegisteredDescriptorById(configurationModel.getDescriptorId())
                   .map(RegisteredDescriptorModel::getName)
                   .orElseThrow(() -> new AlertRuntimeException(MISSING_REGISTERED_DESCRIPTOR_MESSAGE));
    }

}
