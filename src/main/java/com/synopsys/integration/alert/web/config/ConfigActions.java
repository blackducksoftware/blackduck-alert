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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
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
import com.synopsys.integration.exception.IntegrationException;

@Component
public class ConfigActions {
    private final ConfigurationAccessor configurationAccessor;
    private final FieldModelProcessor fieldModelProcessor;
    private final DescriptorProcessor descriptorProcessor;
    private final ConfigurationFieldModelConverter modelConverter;

    @Autowired
    public ConfigActions(ConfigurationAccessor configurationAccessor, FieldModelProcessor fieldModelProcessor, DescriptorProcessor descriptorProcessor, ConfigurationFieldModelConverter modelConverter) {
        this.configurationAccessor = configurationAccessor;
        this.fieldModelProcessor = fieldModelProcessor;
        this.descriptorProcessor = descriptorProcessor;
        this.modelConverter = modelConverter;
    }

    public boolean doesConfigExist(String id) throws AlertException {
        return StringUtils.isNotBlank(id) && doesConfigExist(Long.parseLong(id));
    }

    public boolean doesConfigExist(Long id) throws AlertException {
        return id != null && configurationAccessor.getConfigurationById(id).isPresent();
    }

    public List<FieldModel> getConfigs(ConfigContextEnum context, DescriptorKey descriptorKey) throws AlertException {
        List<FieldModel> fields = new LinkedList<>();
        if (context != null && descriptorKey != null) {
            String contextName = context.name();
            List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey, context);
            List<FieldModel> fieldModelList = new LinkedList<>();
            if (null != configurationModels) {
                for (ConfigurationModel configurationModel : configurationModels) {
                    FieldModel fieldModel = modelConverter.convertToFieldModel(configurationModel);
                    fieldModelList.add(fieldModel);
                }
            }
            if (fieldModelList.isEmpty()) {
                fieldModelList.add(new FieldModel(descriptorKey.getUniversalKey(), contextName, new HashMap<>()));
            }
            for (FieldModel fieldModel : fieldModelList) {
                fields.add(fieldModelProcessor.performAfterReadAction(fieldModel));
            }
        }
        return fields;
    }

    public Optional<FieldModel> getConfigById(Long id) throws AlertException {
        Optional<FieldModel> optionalModel = Optional.empty();
        Optional<ConfigurationModel> configurationModel = configurationAccessor.getConfigurationById(id);
        if (configurationModel.isPresent()) {
            FieldModel configurationFieldModel = modelConverter.convertToFieldModel(configurationModel.get());
            FieldModel fieldModel = fieldModelProcessor.performAfterReadAction(configurationFieldModel);
            optionalModel = Optional.of(fieldModel);
        }
        return optionalModel;
    }

    public void deleteConfig(Long id) throws AlertException {
        if (id != null) {
            Optional<ConfigurationModel> configuration = configurationAccessor.getConfigurationById(id);
            if (configuration.isPresent()) {
                ConfigurationModel configurationModel = configuration.get();
                FieldModel convertedFieldModel = modelConverter.convertToFieldModel(configurationModel);
                FieldModel fieldModel = fieldModelProcessor.performBeforeDeleteAction(convertedFieldModel);
                configurationAccessor.deleteConfiguration(Long.parseLong(fieldModel.getId()));
                fieldModelProcessor.performAfterDeleteAction(fieldModel);
            }
        }
    }

    public FieldModel saveConfig(FieldModel fieldModel, DescriptorKey descriptorKey) throws AlertException {
        validateConfig(fieldModel, new ArrayList<>());
        FieldModel modifiedFieldModel = fieldModelProcessor.performBeforeSaveAction(fieldModel);
        String context = modifiedFieldModel.getContext();
        Map<String, ConfigurationFieldModel> configurationFieldModelMap = modelConverter.convertToConfigurationFieldModelMap(modifiedFieldModel);
        ConfigurationModel configuration = configurationAccessor.createConfiguration(descriptorKey, EnumUtils.getEnum(ConfigContextEnum.class, context), configurationFieldModelMap.values());
        FieldModel dbSavedModel = modelConverter.convertToFieldModel(configuration);
        FieldModel afterSaveAction = fieldModelProcessor.performAfterSaveAction(dbSavedModel);
        return dbSavedModel.fill(afterSaveAction);
    }

    public FieldModel updateConfig(Long id, FieldModel fieldModel) throws AlertException {
        validateConfig(fieldModel, new ArrayList<>());
        Optional<ConfigurationModel> optionalPreviousConfig = configurationAccessor.getConfigurationById(id);
        FieldModel previousFieldModel = optionalPreviousConfig.isPresent() ? modelConverter.convertToFieldModel(optionalPreviousConfig.get()) : null;

        FieldModel updatedFieldModel = fieldModelProcessor.performBeforeUpdateAction(fieldModel);
        Collection<ConfigurationFieldModel> updatedFields = fieldModelProcessor.fillFieldModelWithExistingData(id, updatedFieldModel);
        ConfigurationModel configurationModel = configurationAccessor.updateConfiguration(id, updatedFields);
        FieldModel dbSavedModel = modelConverter.convertToFieldModel(configurationModel);
        FieldModel afterUpdateAction = fieldModelProcessor.performAfterUpdateAction(previousFieldModel, dbSavedModel);
        return dbSavedModel.fill(afterUpdateAction);
    }

    public String validateConfig(FieldModel fieldModel, List<AlertFieldStatus> fieldErrors) throws AlertFieldException {
        fieldErrors.addAll(fieldModelProcessor.validateFieldModel(fieldModel));
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    public String testConfig(FieldModel restModel) throws IntegrationException {
        validateConfig(restModel, new ArrayList<>());
        Optional<TestAction> testActionOptional = descriptorProcessor.retrieveTestAction(restModel);
        if (testActionOptional.isPresent()) {
            FieldModel upToDateFieldModel = fieldModelProcessor.createCustomMessageFieldModel(restModel);
            FieldAccessor fieldAccessor = modelConverter.convertToFieldAccessor(upToDateFieldModel);
            TestAction testAction = testActionOptional.get();

            // TODO return the message from the result of testAction.testConfig(...)
            testAction.testConfig(upToDateFieldModel.getId(), upToDateFieldModel, fieldAccessor);
            return "Successfully sent test message.";
        }
        String descriptorName = restModel.getDescriptorName();
        throw new AlertMethodNotAllowedException("Test functionality not implemented for " + descriptorName);
    }
}
