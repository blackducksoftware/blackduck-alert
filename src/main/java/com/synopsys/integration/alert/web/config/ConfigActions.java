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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class ConfigActions {
    private static final Logger logger = LoggerFactory.getLogger(ConfigActions.class);
    private final ContentConverter contentConverter;
    private final BaseConfigurationAccessor configurationAccessor;
    private final BaseDescriptorAccessor descriptorAccessor;
    private final DescriptorMap descriptorMap;

    @Autowired
    public ConfigActions(final ContentConverter contentConverter, final BaseConfigurationAccessor configurationAccessor, final BaseDescriptorAccessor descriptorAccessor, final DescriptorMap descriptorMap) {
        this.contentConverter = contentConverter;
        this.configurationAccessor = configurationAccessor;
        this.descriptorAccessor = descriptorAccessor;
        this.descriptorMap = descriptorMap;
    }

    public boolean doesConfigExist(final String id) throws AlertException {
        return doesConfigExist(contentConverter.getLongValue(id));
    }

    public boolean doesConfigExist(final Long id) throws AlertException {
        return id != null && configurationAccessor.getConfigurationById(id).isPresent();
    }

    public List<FieldModel> getConfigs(final ConfigContextEnum context, final String descriptorName) throws AlertException {
        final List<FieldModel> fields = new ArrayList<>();
        if (context != null && StringUtils.isNotBlank(descriptorName)) {
            final List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, context);
            if (configurationModels != null) {
                for (final ConfigurationModel configurationModel : configurationModels) {
                    final FieldModel fieldModel = convertToFieldModel(configurationModel);
                    final DescriptorActionApi descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
                    descriptorActionApi.readConfig(fieldModel);
                    fields.add(fieldModel);
                }
            }
        }
        return fields;
    }

    public Optional<FieldModel> getConfigById(final Long id) throws AlertException {
        Optional<FieldModel> optionalModel = Optional.empty();
        final Optional<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationById(id);
        if (configurationModels.isPresent()) {
            final ConfigurationModel configurationModel = configurationModels.get();
            final FieldModel fieldModel = convertToFieldModel(configurationModel);
            final DescriptorActionApi descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
            descriptorActionApi.readConfig(fieldModel);
            optionalModel = Optional.of(fieldModel);
        }
        return optionalModel;
    }

    public void deleteConfig(final String id) throws AlertException {
        deleteConfig(contentConverter.getLongValue(id));
    }

    public void deleteConfig(final Long id) throws AlertException {
        if (id != null) {
            final Optional<ConfigurationModel> configuration = configurationAccessor.getConfigurationById(id);
            if (configuration.isPresent()) {
                final ConfigurationModel configurationModel = configuration.get();
                final FieldModel fieldModel = convertToFieldModel(configurationModel);
                final DescriptorActionApi descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
                if (null != descriptorActionApi) {
                    descriptorActionApi.deleteConfig(fieldModel);
                } else {
                    logger.error("Could not find a Descriptor with the name: " + fieldModel.getDescriptorName());
                }
                configurationAccessor.deleteConfiguration(id);
            }
        }
    }

    public FieldModel saveConfig(final FieldModel fieldModel) throws AlertException {
        final DescriptorActionApi descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
        FieldModel modelToSave = fieldModel;
        if (null != descriptorActionApi) {
            modelToSave = descriptorActionApi.saveConfig(fieldModel);
        } else {
            logger.error("Could not find a Descriptor with the name: " + fieldModel.getDescriptorName());
        }
        final String descriptorName = modelToSave.getDescriptorName();
        final String context = modelToSave.getContext();
        final Map<String, ConfigurationFieldModel> configurationFieldModelMap = modelToSave.convertToConfigurationFieldModelMap();
        return convertToFieldModel(configurationAccessor.createConfiguration(descriptorName, EnumUtils.getEnum(ConfigContextEnum.class, context), configurationFieldModelMap.values()));
    }

    public String validateConfig(final FieldModel fieldModel, final Map<String, String> fieldErrors) throws AlertFieldException {
        final DescriptorActionApi descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
        if (null != descriptorActionApi) {
            descriptorActionApi.validateConfig(fieldModel, fieldErrors);
        } else {
            logger.error("Could not find a Descriptor with the name: " + fieldModel.getDescriptorName());
        }
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    public String testConfig(final FieldModel restModel, final String destination) throws IntegrationException {
        final DescriptorActionApi descriptorActionApi = retrieveDescriptorActionApi(restModel);
        if (null != descriptorActionApi) {
            final TestConfigModel testConfig = descriptorActionApi.createTestConfigModel(restModel, destination);
            descriptorActionApi.testConfig(testConfig);
            return "Successfully sent test message.";
        } else {
            return "Could not find a Descriptor with the name: " + restModel.getDescriptorName();
        }
    }

    public FieldModel updateConfig(final Long id, final FieldModel fieldModel) throws AlertException {
        final DescriptorActionApi descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
        FieldModel modelToSave = fieldModel;
        if (null != descriptorActionApi) {
            modelToSave = descriptorActionApi.updateConfig(fieldModel);
        } else {
            logger.error("Could not find a Descriptor with the name: " + fieldModel.getDescriptorName());
        }
        if (fieldModel != null) {
            final ConfigurationModel configurationModel = getSavedEntity(id);
            final Map<String, ConfigurationFieldModel> fieldModels = modelToSave.convertToConfigurationFieldModelMap();
            final Collection<ConfigurationFieldModel> updatedFields = updateConfigurationWithSavedConfiguration(fieldModels, configurationModel.getCopyOfFieldList());
            return convertToFieldModel(configurationAccessor.updateConfiguration(id, updatedFields));
        }
        return null;
    }

    public ConfigurationModel getSavedEntity(final Long id) throws AlertException {
        if (null != id) {
            final Optional<ConfigurationModel> configuration = configurationAccessor.getConfigurationById(id);
            if (configuration.isPresent()) {
                return configuration.get();
            }
        }
        return null;
    }

    public Collection<ConfigurationFieldModel> updateConfigurationWithSavedConfiguration(final Map<String, ConfigurationFieldModel> newConfiguration, final Collection<ConfigurationFieldModel> savedConfiguration) throws AlertException {
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

    private FieldModel convertToFieldModel(final ConfigurationModel configurationModel) throws AlertDatabaseConstraintException {
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

    private DescriptorActionApi retrieveDescriptorActionApi(final String context, final String descriptorName) {
        final ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);

        if (descriptorContext != null) {
            final Descriptor descriptor = descriptorMap.getDescriptor(descriptorName);
            return descriptor.getActionApi(descriptorContext).orElse(null);
        }
        return null;
    }

    private DescriptorActionApi retrieveDescriptorActionApi(final FieldModel fieldModel) {
        return retrieveDescriptorActionApi(fieldModel.getContext(), fieldModel.getDescriptorName());
    }

}
