/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.descriptor.accessor.DefaultDescriptorGlobalConfigUtility;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.component.settings.actions.SettingsGlobalApiAction;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class DefaultSettingsUtility implements SettingsUtility {
    private final DefaultDescriptorGlobalConfigUtility configUtility;

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
    public Optional<ConfigurationModel> getConfiguration() {
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
