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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class ConfigActions {
    private final BaseConfigurationAccessor configurationAccessor;
    private FieldModelProcessor fieldModelProcessor;
    private ConfigurationFieldModelConverter modelConverter;
    private ContentConverter contentConverter;

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
        final List<FieldModel> fields = new ArrayList<>();
        if (context != null && StringUtils.isNotBlank(descriptorName)) {
            final List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, context);
            if (configurationModels != null) {
                for (final ConfigurationModel configurationModel : configurationModels) {
                    final FieldModel fieldModel = fieldModelProcessor.performReadAction(configurationModel);
                    fields.add(fieldModel);
                }
            }
        }
        return fields;
    }

    public Optional<FieldModel> getConfigById(final Long id) throws AlertException {
        Optional<FieldModel> optionalModel = Optional.empty();
        final Optional<ConfigurationModel> configurationModel = configurationAccessor.getConfigurationById(id);
        if (configurationModel.isPresent()) {
            final FieldModel fieldModel = fieldModelProcessor.performReadAction(configurationModel.get());
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
        final String descriptorName = fieldModel.getDescriptorName();
        final String context = fieldModel.getContext();
        final Map<String, ConfigurationFieldModel> configurationFieldModelMap = modelConverter.convertFromFieldModel(fieldModel);
        final ConfigurationModel configuration = configurationAccessor.createConfiguration(descriptorName, EnumUtils.getEnum(ConfigContextEnum.class, context), configurationFieldModelMap.values());
        final FieldModel dbSavedModel = fieldModelProcessor.convertToFieldModel(configuration);
        final FieldModel combinedModel = dbSavedModel.fill(fieldModel);
        return fieldModelProcessor.performSaveAction(combinedModel);
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
        return fieldModelProcessor.testFieldModel(restModel, destination);
    }

    public FieldModel updateConfig(final Long id, final FieldModel fieldModel) throws AlertException, AlertFieldException {
        validateConfig(fieldModel, new HashMap<>());
        final Collection<ConfigurationFieldModel> updatedFields = fieldModelProcessor.fillFieldModelWithExistingData(id, fieldModel);
        final ConfigurationModel configurationModel = configurationAccessor.updateConfiguration(id, updatedFields);
        FieldModel dbSavedModel = fieldModelProcessor.convertToFieldModel(configurationModel);
        FieldModel combinedModel = dbSavedModel.fill(fieldModel);
        return fieldModelProcessor.performUpdateAction(combinedModel);
    }

}
