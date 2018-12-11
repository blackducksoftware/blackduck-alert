/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.web.config.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.database.FieldConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DescriptorAccessor.RegisteredDescriptorModel;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class DescriptorConfigActions {
    private final ContentConverter contentConverter;
    private final FieldConfigurationAccessor configurationAccessor;
    private final BaseDescriptorAccessor descriptorAccessor;
    private final DescriptorMap descriptorMap;

    @Autowired
    public DescriptorConfigActions(final ContentConverter contentConverter, final FieldConfigurationAccessor configurationAccessor, final BaseDescriptorAccessor descriptorAccessor, final DescriptorMap descriptorMap) {
        this.contentConverter = contentConverter;
        this.configurationAccessor = configurationAccessor;
        this.descriptorAccessor = descriptorAccessor;
        this.descriptorMap = descriptorMap;
    }

    public boolean doesConfigExist(final String id) throws AlertException {
        return doesConfigExist(contentConverter.getLongValue(id));
    }

    public boolean doesConfigExist(final Long id) throws AlertException {
        return id != null && !configurationAccessor.getConfigurationById(id).isEmpty();
    }

    public List<FieldModel> getConfigs(final ConfigContextEnum context, final String descriptorName) throws AlertException {
        final List<FieldModel> fields = new ArrayList<>();
        if (context != null && StringUtils.isNotBlank(descriptorName)) {
            final List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, context);
            if (configurationModels != null) {
                for (final ConfigurationModel configurationModel : configurationModels) {
                    fields.add(convertToFieldModel(configurationModel));
                }
            }
        }
        return fields;
    }

    public FieldModel getConfigById(final Long id) throws AlertException {
        final List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationById(id);
        if (!configurationModels.isEmpty()) {
            final ConfigurationModel configurationModel = configurationModels.get(0);
            if (configurationModel != null) {
                return convertToFieldModel(configurationModel);
            }
        }
        return null;
    }

    public void deleteConfig(final String id) throws AlertException {
        deleteConfig(contentConverter.getLongValue(id));
    }

    public void deleteConfig(final Long id) throws AlertException {
        if (id != null) {
            configurationAccessor.deleteConfiguration(id);
        }
    }

    public ConfigurationModel saveConfig(final FieldModel fieldModel, final ConfigContextEnum context) throws AlertException {
        final String descriptorName = fieldModel.getDescriptorName();
        final Map<String, ConfigurationFieldModel> configurationFieldModelMap = fieldModel.convertToConfigurationFieldModelMap();
        return configurationAccessor.createConfiguration(descriptorName, context, configurationFieldModelMap.values());
    }

    public String validateConfig(final FieldModel fieldModel, final Map<String, String> fieldErrors) throws AlertFieldException {
        final DescriptorActionApi descriptorActionApi = retrieveDescriptorActionApi(fieldModel);
        descriptorActionApi.validateConfig(fieldModel.convertToFieldAccessor(), fieldErrors);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    public String testConfig(final FieldModel restModel, final String destination) throws IntegrationException {
        final DescriptorActionApi descriptorActionApi = retrieveDescriptorActionApi(restModel);
        final TestConfigModel testConfig = descriptorActionApi.createTestConfigModel(restModel, destination);
        descriptorActionApi.testConfig(testConfig);
        return "Successfully sent test message.";
    }

    public ConfigurationModel updateConfig(final FieldModel fieldModel) throws AlertException {
        final String id = fieldModel.getId();
        if (fieldModel != null && StringUtils.isNotBlank(id)) {
            final Long longId = contentConverter.getLongValue(id);
            final ConfigurationModel configurationModel = getSavedEntity(longId);
            final Map<String, ConfigurationFieldModel> fieldModels = fieldModel.convertToConfigurationFieldModelMap();
            final Collection<ConfigurationFieldModel> updatedFields = updateConfigurationWithSavedConfiguration(fieldModels, configurationModel.getCopyOfFieldList());
            return configurationAccessor.updateConfiguration(longId, updatedFields);
        }
        return null;
    }

    public ConfigurationModel getSavedEntity(final Long id) throws AlertException {
        if (null != id) {
            final List<ConfigurationModel> configuration = configurationAccessor.getConfigurationById(id);
            if (!configuration.isEmpty()) {
                return configuration.get(0);
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

    private DescriptorActionApi retrieveDescriptorActionApi(final FieldModel fieldModel) {
        final ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, fieldModel.getContext());

        if (descriptorContext != null) {
            final Descriptor descriptor = descriptorMap.getDescriptor(fieldModel.getDescriptorName());
            return descriptor.getRestApi(descriptorContext);
        }
        return null;
    }

}
