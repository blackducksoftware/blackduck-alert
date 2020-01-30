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
package com.synopsys.integration.alert.component.settings;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.component.settings.actions.SettingsGlobalApiAction;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;

@Component
public class DefaultSettingsUtility implements SettingsUtility {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConfigurationAccessor configurationAccessor;
    private final SettingsGlobalApiAction apiAction;
    private final ConfigurationFieldModelConverter configurationFieldModelConverter;
    private DescriptorKey key;
    private ConfigContextEnum context;

    @Autowired
    public DefaultSettingsUtility(SettingsDescriptorKey settingsDescriptorKey, ConfigurationAccessor configurationAccessor, SettingsGlobalApiAction settingsGlobalApiAction,
        ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.key = settingsDescriptorKey;
        this.context = ConfigContextEnum.GLOBAL;
        this.configurationAccessor = configurationAccessor;
        this.apiAction = settingsGlobalApiAction;
        this.configurationFieldModelConverter = configurationFieldModelConverter;
    }

    @Override
    public DescriptorKey getKey() {
        return key;
    }

    @Override
    public boolean doesConfigurationExist() {
        try {
            return !configurationAccessor.getConfigurationByDescriptorKeyAndContext(key, context).isEmpty();
        } catch (AlertException ex) {
            logger.debug("Error reading configuration from database.", ex);
            return false;
        }
    }

    @Override
    public Optional<ConfigurationModel> getConfiguration() throws AlertException {
        return configurationAccessor.getConfigurationByDescriptorKeyAndContext(getKey(), ConfigContextEnum.GLOBAL)
                   .stream()
                   .findFirst();
    }

    @Override
    public Optional<FieldModel> getFieldModel() throws AlertException {
        Optional<ConfigurationModel> configurationModelOptional = getConfiguration();

        if (configurationModelOptional.isPresent()) {
            ConfigurationModel configurationModel = configurationModelOptional.get();
            FieldModel fieldModel = configurationFieldModelConverter.convertToFieldModel(configurationModel);
            return Optional.ofNullable(apiAction.afterGetAction(fieldModel));
        }

        return Optional.empty();
    }

    @Override
    public FieldModel saveSettings(FieldModel fieldModel) throws AlertException {
        FieldModel beforeAction = apiAction.beforeSaveAction(fieldModel);
        Collection<ConfigurationFieldModel> values = configurationFieldModelConverter.convertToConfigurationFieldModelMap(beforeAction).values();
        ConfigurationModel configuration = configurationAccessor.createConfiguration(getKey(), ConfigContextEnum.GLOBAL, values);
        FieldModel convertedFieldModel = configurationFieldModelConverter.convertToFieldModel(configuration);
        return apiAction.afterSaveAction(convertedFieldModel);
    }

    @Override
    public FieldModel updateSettings(Long id, FieldModel fieldModel) throws AlertException {
        FieldModel beforeUpdateAction = apiAction.beforeUpdateAction(fieldModel);
        Collection<ConfigurationFieldModel> values = configurationFieldModelConverter.convertToConfigurationFieldModelMap(beforeUpdateAction).values();
        ConfigurationModel configurationModel = configurationAccessor.updateConfiguration(id, values);
        FieldModel convertedFieldModel = configurationFieldModelConverter.convertToFieldModel(configurationModel);
        return apiAction.afterUpdateAction(convertedFieldModel);
    }
}
