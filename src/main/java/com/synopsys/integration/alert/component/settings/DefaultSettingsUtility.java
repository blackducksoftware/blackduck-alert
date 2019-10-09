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
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;

@Component
public class DefaultSettingsUtility implements SettingsUtility {
    private SettingsDescriptorKey settingsDescriptorKey;
    private ConfigurationAccessor configurationAccessor;
    private DescriptorAccessor descriptorAccessor;

    @Autowired
    public DefaultSettingsUtility(SettingsDescriptorKey settingsDescriptorKey, ConfigurationAccessor configurationAccessor, final DescriptorAccessor descriptorAccessor) {
        this.settingsDescriptorKey = settingsDescriptorKey;
        this.configurationAccessor = configurationAccessor;
        this.descriptorAccessor = descriptorAccessor;
    }

    @Override
    public String getSettingsName() {
        return settingsDescriptorKey.getUniversalKey();
    }

    @Override
    public Optional<ConfigurationModel> getSettings() throws AlertException {
        return configurationAccessor.getConfigurationByDescriptorNameAndContext(settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL).stream().findFirst();
    }

    @Override
    public ConfigurationModel saveSettings(final Collection<ConfigurationFieldModel> fieldModels) throws AlertException {
        return configurationAccessor.createConfiguration(settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL, fieldModels);
    }

    @Override
    public ConfigurationModel updateSettings(final Long id, final Collection<ConfigurationFieldModel> fieldModels) throws AlertException {
        return configurationAccessor.updateConfiguration(id, fieldModels);
    }

    @Override
    public List<DefinedFieldModel> getSettingsFields() throws AlertException {
        return descriptorAccessor.getFieldsForDescriptor(settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL);
    }
}
