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

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.accessor.DefaultDescriptorGlobalConfigUtility;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.component.settings.actions.SettingsGlobalApiAction;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;

@Component
public class DefaultSettingsUtility implements SettingsUtility {
    private DefaultDescriptorGlobalConfigUtility configUtility;

    @Autowired
    public DefaultSettingsUtility(SettingsDescriptorKey settingsDescriptorKey, ConfigurationAccessor configurationAccessor, SettingsGlobalApiAction settingsGlobalApiAction,
        ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.configUtility = new DefaultDescriptorGlobalConfigUtility(settingsDescriptorKey, configurationAccessor, settingsGlobalApiAction, configurationFieldModelConverter);
    }

    @Override
    public DescriptorKey getKey() {
        return configUtility.getKey();
    }

    @Override
    public boolean doesConfigurationExist() {
        return configUtility.doesConfigurationExist();
    }

    @Override
    public Optional<ConfigurationModel> getConfiguration() throws AlertException {
        return configUtility.getConfiguration();
    }

    @Override
    public Optional<FieldModel> getFieldModel() throws AlertException {
        return configUtility.getFieldModel();
    }

    @Override
    public FieldModel saveSettings(FieldModel fieldModel) throws AlertException {
        return configUtility.save(fieldModel);
    }

    @Override
    public FieldModel updateSettings(Long id, FieldModel fieldModel) throws AlertException {
        return configUtility.update(id, fieldModel);
    }
}
