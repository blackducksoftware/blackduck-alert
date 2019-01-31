/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.web.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;
import com.synopsys.integration.alert.web.model.configuration.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class FieldModelProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FieldModelProcessor.class);
    private BaseDescriptorAccessor descriptorAccessor;
    private BaseConfigurationAccessor configurationAccessor;
    private DescriptorMap descriptorMap;
    private ConfigurationFieldModelConverter fieldModelConverter;

    @Autowired
    public FieldModelProcessor(final BaseDescriptorAccessor descriptorAccessor, final BaseConfigurationAccessor configurationAccessor, final DescriptorMap descriptorMap,
        final ConfigurationFieldModelConverter fieldModelConverter) {
        this.descriptorAccessor = descriptorAccessor;
        this.configurationAccessor = configurationAccessor;
        this.descriptorMap = descriptorMap;
        this.fieldModelConverter = fieldModelConverter;
    }

    public FieldModel readFieldModel(ConfigurationModel configurationModel) throws AlertDatabaseConstraintException {
        FieldModel fieldModel = convertToFieldModel(configurationModel);
        final Optional<DescriptorActionApi> descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
        return descriptorActionApi.map(actionApi -> actionApi.readConfig(fieldModel)).orElse(fieldModel);
    }

    public FieldModel deleteFieldModel(ConfigurationModel configurationModel) throws AlertDatabaseConstraintException {
        final FieldModel fieldModel = convertToFieldModel(configurationModel);
        final Optional<DescriptorActionApi> descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
        return descriptorActionApi.map(actionApi -> actionApi.deleteConfig(fieldModel)).orElse(fieldModel);
    }

    public FieldModel saveFieldModel(FieldModel fieldModel) {
        final Optional<DescriptorActionApi> descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
        return descriptorActionApi.map(actionApi -> actionApi.saveConfig(fieldModel)).orElse(fieldModel);
    }

    public Map<String, String> validateFieldModel(FieldModel fieldModel) {
        Map<String, String> fieldErrors = new HashMap<>();
        final Optional<DescriptorActionApi> descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
        if (descriptorActionApi.isPresent()) {
            final Map<String, ConfigField> configFields = retrieveUIConfigFields(fieldModel.getContext(), fieldModel.getDescriptorName())
                                                              .stream()
                                                              .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
            descriptorActionApi.get().validateConfig(configFields, fieldModel, fieldErrors);
        }
        return fieldErrors;
    }

    public String testFieldModel(FieldModel fieldModel, String destination) throws IntegrationException {
        final Optional<DescriptorActionApi> descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
        if (descriptorActionApi.isPresent()) {
            final DescriptorActionApi descriptorApi = descriptorActionApi.get();
            final FieldAccessor fieldAccessor = fieldModelConverter.convertToFieldAccessor(fieldModel);
            final TestConfigModel testConfig = descriptorApi.createTestConfigModel(fieldModel.getId(), fieldAccessor, destination);
            descriptorApi.testConfig(testConfig);
            return "Successfully sent test message.";
        } else {
            logger.error("Descriptor action api did not exist: {}", fieldModel.getDescriptorName());
            return "Internal server error. Failed to send test message.";
        }
    }

    public Collection<ConfigurationFieldModel> updateFieldModel(Long id, FieldModel fieldModel) throws AlertException {
        final Optional<DescriptorActionApi> descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
        FieldModel modelToSave = descriptorActionApi.map(actionApi -> actionApi.updateConfig(fieldModel)).orElse(fieldModel);
        final Optional<ConfigurationModel> configurationModel = getSavedEntity(id);
        if (configurationModel.isPresent()) {
            final Map<String, ConfigurationFieldModel> fieldModels = fieldModelConverter.convertFromFieldModel(modelToSave);
            return updateConfigurationWithSavedConfiguration(fieldModels, configurationModel.get().getCopyOfFieldList());
        }

        return fieldModelConverter.convertFromFieldModel(modelToSave).values();
    }

    public Optional<Descriptor> retrieveDescriptor(final String descriptorName) {
        return descriptorMap.getDescriptor(descriptorName);
    }

    public Optional<DescriptorActionApi> retrieveDescriptorActionApi(final FieldModel fieldModel) {
        return retrieveDescriptorActionApi(fieldModel.getContext(), fieldModel.getDescriptorName());
    }

    public Optional<DescriptorActionApi> retrieveDescriptorActionApi(final String context, final String descriptorName) {
        final Optional<Descriptor> descriptor = retrieveDescriptor(descriptorName);
        final ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
        if (descriptor.isPresent()) {
            return Optional.ofNullable(descriptor.get().getActionApi(descriptorContext).orElse(null));
        }
        logger.error("Could not find a Descriptor with the name: " + descriptorName);
        return Optional.empty();
    }

    public FieldModel convertToFieldModel(final ConfigurationModel configurationModel) throws AlertDatabaseConstraintException {
        final Long configId = configurationModel.getConfigurationId();
        final RegisteredDescriptorModel descriptor = descriptorAccessor.getRegisteredDescriptorById(configurationModel.getDescriptorId())
                                                         .orElseThrow(() -> new AlertDatabaseConstraintException("Expected to find registered descriptor but none was found."));
        final String descriptorName = descriptor.getName();
        final Map<String, FieldValueModel> fields = new HashMap<>();
        for (final ConfigurationFieldModel fieldModel : configurationModel.getCopyOfFieldList()) {
            populateAndSecureFields(fieldModel, fields);
        }

        return new FieldModel(configId.toString(), descriptorName, configurationModel.getDescriptorContext().name(), fields);
    }

    private void populateAndSecureFields(final ConfigurationFieldModel fieldModel, final Map<String, FieldValueModel> fields) {
        final String key = fieldModel.getFieldKey();
        Collection<String> values = Collections.emptyList();
        if (!fieldModel.isSensitive()) {
            values = fieldModel.getFieldValues();
        }
        final FieldValueModel fieldValueModel = new FieldValueModel(values, fieldModel.isSet());
        fields.put(key, fieldValueModel);
    }

    private List<ConfigField> retrieveUIConfigFields(final String context, String descriptorName) {
        final ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
        return retrieveUIConfigFields(descriptorContext, descriptorName);
    }

    private List<ConfigField> retrieveUIConfigFields(final ConfigContextEnum context, final String descriptorName) {
        final Optional<Descriptor> optionalDescriptor = retrieveDescriptor(descriptorName);
        final List<ConfigField> fieldsToReturn = new LinkedList<>();
        if (optionalDescriptor.isPresent()) {
            final Descriptor descriptor = optionalDescriptor.get();
            final Optional<UIConfig> uiConfig = descriptor.getUIConfig(context);
            fieldsToReturn.addAll(uiConfig.map(config -> config.createFields()).orElse(List.of()));
        }
        return fieldsToReturn;
    }

    public Optional<ConfigurationModel> getSavedEntity(final Long id) throws AlertException {
        if (null != id) {
            return configurationAccessor.getConfigurationById(id);
        }
        return Optional.empty();
    }

    private Collection<ConfigurationFieldModel> updateConfigurationWithSavedConfiguration(final Map<String, ConfigurationFieldModel> newConfiguration, final Collection<ConfigurationFieldModel> savedConfiguration) throws AlertException {
        final Collection<ConfigurationFieldModel> sensitiveFields = savedConfiguration.stream().filter(fieldModel -> fieldModel.isSensitive()).collect(Collectors.toSet());
        for (final ConfigurationFieldModel fieldModel : sensitiveFields) {
            final String key = fieldModel.getFieldKey();
            if (newConfiguration.containsKey(key)) {
                final ConfigurationFieldModel newFieldModel = newConfiguration.get(key);
                newFieldModel.setFieldValues(fieldModel.getFieldValues());
            }
        }
        return newConfiguration.values();
    }

}
