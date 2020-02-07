/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertMethodNotAllowedException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class ConfigActions {
    private static final Logger logger = LoggerFactory.getLogger(ConfigActions.class);
    private final ConfigurationAccessor configurationAccessor;
    private final FieldModelProcessor fieldModelProcessor;
    private final DescriptorProcessor descriptorProcessor;
    private final ConfigurationFieldModelConverter modelConverter;

    @Autowired
    public ConfigActions(final ConfigurationAccessor configurationAccessor, final FieldModelProcessor fieldModelProcessor, final DescriptorProcessor descriptorProcessor, final ConfigurationFieldModelConverter modelConverter) {
        this.configurationAccessor = configurationAccessor;
        this.fieldModelProcessor = fieldModelProcessor;
        this.descriptorProcessor = descriptorProcessor;
        this.modelConverter = modelConverter;
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
            final List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, context);
            final List<FieldModel> fieldModelList = new LinkedList<>();
            if (null != configurationModels) {
                for (final ConfigurationModel configurationModel : configurationModels) {
                    final FieldModel fieldModel = modelConverter.convertToFieldModel(configurationModel);
                    fieldModelList.add(fieldModel);
                }
            }

            if (fieldModelList.isEmpty()) {
                fieldModelList.add(new FieldModel(descriptorName, contextName, new HashMap<>()));
            }

            for (final FieldModel fieldModel : fieldModelList) {
                fields.add(fieldModelProcessor.performAfterReadAction(fieldModel));
            }
        }
        return fields;
    }

    public Optional<FieldModel> getConfigById(final Long id) throws AlertException {
        Optional<FieldModel> optionalModel = Optional.empty();
        final Optional<ConfigurationModel> configurationModel = configurationAccessor.getConfigurationById(id);
        if (configurationModel.isPresent()) {
            final FieldModel configurationFieldModel = modelConverter.convertToFieldModel(configurationModel.get());
            final FieldModel fieldModel = fieldModelProcessor.performAfterReadAction(configurationFieldModel);
            optionalModel = Optional.of(fieldModel);
        }
        return optionalModel;
    }

    public void deleteConfig(final Long id) throws AlertException {
        if (id != null) {
            final Optional<ConfigurationModel> configuration = configurationAccessor.getConfigurationById(id);
            if (configuration.isPresent()) {
                final ConfigurationModel configurationModel = configuration.get();
                final FieldModel convertedFieldModel = modelConverter.convertToFieldModel(configurationModel);
                final FieldModel fieldModel = fieldModelProcessor.performBeforeDeleteAction(convertedFieldModel);
                final String descriptorName = fieldModel.getDescriptorName();
                final String context = fieldModel.getContext();
                configurationAccessor.deleteConfiguration(Long.parseLong(fieldModel.getId()));
                fieldModelProcessor.performAfterDeleteAction(descriptorName, context);
            }
        }
    }

    public FieldModel saveConfig(final FieldModel fieldModel) throws AlertException {
        validateConfig(fieldModel, new HashMap<>());
        final FieldModel modifiedFieldModel = fieldModelProcessor.performBeforeSaveAction(fieldModel);
        final String descriptorName = modifiedFieldModel.getDescriptorName();
        final String context = modifiedFieldModel.getContext();
        final Map<String, ConfigurationFieldModel> configurationFieldModelMap = modelConverter.convertToConfigurationFieldModelMap(modifiedFieldModel);
        final ConfigurationModel configuration = configurationAccessor.createConfiguration(descriptorName, EnumUtils.getEnum(ConfigContextEnum.class, context), configurationFieldModelMap.values());
        final FieldModel dbSavedModel = modelConverter.convertToFieldModel(configuration);
        final FieldModel afterSaveAction = fieldModelProcessor.performAfterSaveAction(dbSavedModel);
        return dbSavedModel.fill(afterSaveAction);
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
        final Optional<TestAction> testActionOptional = descriptorProcessor.retrieveTestAction(restModel);
        if (testActionOptional.isPresent()) {
            final FieldModel upToDateFieldModel = fieldModelProcessor.createTestFieldModel(restModel);
            final FieldAccessor fieldAccessor = modelConverter.convertToFieldAccessor(upToDateFieldModel);
            final TestAction testAction = testActionOptional.get();
            final TestConfigModel testConfig = testAction.createTestConfigModel(upToDateFieldModel.getId(), fieldAccessor, destination);
            testAction.testConfig(testConfig);
            return "Successfully sent test message.";
        }
        final String descriptorName = restModel.getDescriptorName();
        logger.error("Test action did not exist: {}", descriptorName);
        throw new AlertMethodNotAllowedException("Test functionality not implemented for " + descriptorName);
    }

    public FieldModel updateConfig(final Long id, final FieldModel fieldModel) throws AlertException {
        validateConfig(fieldModel, new HashMap<>());
        final FieldModel updatedFieldModel = fieldModelProcessor.performBeforeUpdateAction(fieldModel);
        final Collection<ConfigurationFieldModel> updatedFields = fieldModelProcessor.fillFieldModelWithExistingData(id, updatedFieldModel);
        final ConfigurationModel configurationModel = configurationAccessor.updateConfiguration(id, updatedFields);
        final FieldModel dbSavedModel = modelConverter.convertToFieldModel(configurationModel);
        final FieldModel afterUpdateAction = fieldModelProcessor.performAfterUpdateAction(dbSavedModel);
        return dbSavedModel.fill(afterUpdateAction);
    }

}
