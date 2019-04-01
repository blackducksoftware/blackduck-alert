/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class ConfigActions {
    private static final Logger logger = LoggerFactory.getLogger(ConfigActions.class);
    private final BaseConfigurationAccessor configurationAccessor;
    private final FieldModelProcessor fieldModelProcessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final ContentConverter contentConverter;

    @Autowired
    public ConfigActions(final BaseConfigurationAccessor configurationAccessor, final FieldModelProcessor fieldModelProcessor, final ConfigurationFieldModelConverter modelConverter,
        final ContentConverter contentConverter) {
        this.configurationAccessor = configurationAccessor;
        this.fieldModelProcessor = fieldModelProcessor;
        this.modelConverter = modelConverter;
        this.contentConverter = contentConverter;
    }

    public boolean doesConfigExist(final String id) throws AlertException {
        return StringUtils.isNotBlank(id) && doesConfigExist(Long.parseLong(id));
    }

    public boolean doesConfigExist(final Long id) throws AlertException {
        return id != null && configurationAccessor.getConfigurationById(id).isPresent();
    }

    public List<FieldModel> getConfigs(final ConfigContextEnum context, final String descriptorName) throws AlertException {
        final List<FieldModel> fields = new LinkedList<>();
        if (context != null && StringUtils.isNotBlank(descriptorName)) {
            final String contextName = context.name();
            final Optional<DescriptorActionApi> descriptorActionApi = fieldModelProcessor.retrieveDescriptorActionApi(contextName, descriptorName);
            final List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, context);
            final List<FieldModel> fieldModelList = new LinkedList<>();
            if (null != configurationModels) {
                for (final ConfigurationModel configurationModel : configurationModels) {
                    final FieldModel fieldModel = fieldModelProcessor.convertToFieldModel(configurationModel);
                    fieldModelList.add(fieldModel);
                }
            }

            if (descriptorActionApi.isPresent() && fieldModelList.isEmpty()) {
                fieldModelList.add(new FieldModel(descriptorName, contextName, new HashMap<>()));
            }

            for (final FieldModel fieldModel : fieldModelList) {
                fields.add(fieldModelProcessor.performReadAction(fieldModel));
            }
        }
        return fields;
    }

    public Optional<FieldModel> getConfigById(final Long id) throws AlertException {
        Optional<FieldModel> optionalModel = Optional.empty();
        final Optional<ConfigurationModel> configurationModel = configurationAccessor.getConfigurationById(id);
        if (configurationModel.isPresent()) {
            final FieldModel configurationFieldModel = fieldModelProcessor.convertToFieldModel(configurationModel.get());
            final FieldModel fieldModel = fieldModelProcessor.performReadAction(configurationFieldModel);
            optionalModel = Optional.of(fieldModel);
        }
        return optionalModel;
    }

    public void deleteConfig(final Long id) throws AlertException {
        if (id != null) {
            final Optional<ConfigurationModel> configuration = configurationAccessor.getConfigurationById(id);
            if (configuration.isPresent()) {
                final ConfigurationModel configurationModel = configuration.get();
                final FieldModel fieldModel = fieldModelProcessor.performDeleteAction(configurationModel);
                configurationAccessor.deleteConfiguration(contentConverter.getLongValue(fieldModel.getId()));
            }
        }
    }

    public FieldModel saveConfig(final FieldModel fieldModel) throws AlertException, AlertFieldException {
        validateConfig(fieldModel, new HashMap<>());
        final FieldModel modifiedFieldModel = fieldModelProcessor.performSaveAction(fieldModel);
        final String descriptorName = modifiedFieldModel.getDescriptorName();
        final String context = modifiedFieldModel.getContext();
        final Map<String, ConfigurationFieldModel> configurationFieldModelMap = modelConverter.convertFromFieldModel(modifiedFieldModel);
        final ConfigurationModel configuration = configurationAccessor.createConfiguration(descriptorName, EnumUtils.getEnum(ConfigContextEnum.class, context), configurationFieldModelMap.values());
        final FieldModel dbSavedModel = fieldModelProcessor.convertToFieldModel(configuration);
        return dbSavedModel.fill(modifiedFieldModel);
    }

    public String validateConfig(final FieldModel fieldModel, final Map<String, String> fieldErrors) throws AlertFieldException {
        fieldErrors.putAll(fieldModelProcessor.validateFieldModel(fieldModel));
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    public String testConfig(final FieldModel restModel, final String destination) throws IntegrationException {
        validateConfig(restModel, new HashMap<>());
        final Optional<DescriptorActionApi> descriptorActionApi = fieldModelProcessor.retrieveDescriptorActionApi(restModel);
        if (descriptorActionApi.isPresent()) {
            final DescriptorActionApi descriptorApi = descriptorActionApi.get();
            final FieldModel upToDateFieldModel = fieldModelProcessor.createTestFieldModel(restModel);
            final FieldAccessor fieldAccessor = modelConverter.convertToFieldAccessor(upToDateFieldModel);
            final TestConfigModel testConfig = descriptorApi.createTestConfigModel(upToDateFieldModel.getId(), fieldAccessor, destination);
            descriptorApi.testConfig(testConfig);
            return "Successfully sent test message.";
        } else {
            logger.error("Descriptor action api did not exist: {}", restModel.getDescriptorName());
            return "Internal server error. Failed to send test message.";
        }
    }

    public FieldModel updateConfig(final Long id, final FieldModel fieldModel) throws AlertException, AlertFieldException {
        validateConfig(fieldModel, new HashMap<>());
        final FieldModel updatedFieldModel = fieldModelProcessor.performUpdateAction(fieldModel);
        final Collection<ConfigurationFieldModel> updatedFields = fieldModelProcessor.fillFieldModelWithExistingData(id, updatedFieldModel);
        final ConfigurationModel configurationModel = configurationAccessor.updateConfiguration(id, updatedFields);
        final FieldModel dbSavedModel = fieldModelProcessor.convertToFieldModel(configurationModel);
        return dbSavedModel.fill(updatedFieldModel);
    }

}
