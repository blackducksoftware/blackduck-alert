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
package com.synopsys.integration.alert.component.settings;

import java.util.Collection;
import java.util.Optional;

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
    private SettingsDescriptorKey settingsDescriptorKey;
    private ConfigurationAccessor configurationAccessor;
    private SettingsGlobalApiAction settingsGlobalApiAction;
    private ConfigurationFieldModelConverter configurationFieldModelConverter;

    @Autowired
    public DefaultSettingsUtility(SettingsDescriptorKey settingsDescriptorKey, ConfigurationAccessor configurationAccessor, SettingsGlobalApiAction settingsGlobalApiAction,
        ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.settingsDescriptorKey = settingsDescriptorKey;
        this.configurationAccessor = configurationAccessor;
        this.settingsGlobalApiAction = settingsGlobalApiAction;
        this.configurationFieldModelConverter = configurationFieldModelConverter;
    }

    @Override
    public DescriptorKey getSettingsKey() {
        return settingsDescriptorKey;
    }

    @Override
    public boolean doSettingsExist() throws AlertException {
        return !configurationAccessor.getConfigurationByDescriptorKeyAndContext(settingsDescriptorKey, ConfigContextEnum.GLOBAL).isEmpty();
    }

    @Override
    public Optional<FieldModel> getSettingsFieldModel() throws AlertException {
        final Optional<ConfigurationModel> configurationModelOptional = configurationAccessor.getConfigurationByDescriptorKeyAndContext(settingsDescriptorKey, ConfigContextEnum.GLOBAL)
                                                                            .stream()
                                                                            .findFirst();

        if (configurationModelOptional.isPresent()) {
            ConfigurationModel configurationModel = configurationModelOptional.get();
            FieldModel fieldModel = configurationFieldModelConverter.convertToFieldModel(configurationModel);
            return Optional.ofNullable(settingsGlobalApiAction.afterGetAction(fieldModel));
        }

        return Optional.empty();
    }

    @Override
    public Optional<ConfigurationModel> getSettings() throws AlertException {
        Optional<FieldModel> fieldModel = getSettingsFieldModel();

        if (fieldModel.isPresent()) {
            ConfigurationModel configurationModel = configurationFieldModelConverter.convertToConfigurationModel(fieldModel.get());
            return Optional.ofNullable(configurationModel);
        }

        return Optional.empty();
    }

    @Override
    public FieldModel saveSettings(final FieldModel fieldModel) throws AlertException {
        FieldModel beforeAction = settingsGlobalApiAction.beforeSaveAction(fieldModel);
        Collection<ConfigurationFieldModel> values = configurationFieldModelConverter.convertToConfigurationFieldModelMap(beforeAction).values();
        ConfigurationModel configuration = configurationAccessor.createConfiguration(settingsDescriptorKey, ConfigContextEnum.GLOBAL, values);
        FieldModel convertedFieldModel = configurationFieldModelConverter.convertToFieldModel(configuration);
        return settingsGlobalApiAction.afterSaveAction(convertedFieldModel);
    }

    @Override
    public FieldModel updateSettings(final Long id, final FieldModel fieldModel) throws AlertException {
        FieldModel beforeUpdateAction = settingsGlobalApiAction.beforeUpdateAction(fieldModel);
        Collection<ConfigurationFieldModel> values = configurationFieldModelConverter.convertToConfigurationFieldModelMap(beforeUpdateAction).values();
        ConfigurationModel configurationModel = configurationAccessor.updateConfiguration(id, values);
        FieldModel convertedFieldModel = configurationFieldModelConverter.convertToFieldModel(configurationModel);
        return settingsGlobalApiAction.afterUpdateAction(convertedFieldModel);
    }

}
