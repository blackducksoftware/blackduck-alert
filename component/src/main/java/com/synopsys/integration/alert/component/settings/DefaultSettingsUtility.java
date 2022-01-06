/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class DefaultSettingsUtility implements SettingsUtility {
    SettingsProxyConfigAccessor settingsProxyConfigAccessor;
    SettingsDescriptorKey settingsDescriptorKey;

    @Autowired
    public DefaultSettingsUtility(SettingsProxyConfigAccessor settingsProxyConfigAccessor, SettingsDescriptorKey settingsDescriptorKey) {
        this.settingsProxyConfigAccessor = settingsProxyConfigAccessor;
        this.settingsDescriptorKey = settingsDescriptorKey;
    }

    @Override
    public DescriptorKey getKey() {
        return settingsDescriptorKey;
    }

    @Override
    public Optional<SettingsProxyModel> getConfiguration() {
        return settingsProxyConfigAccessor.getConfiguration();
    }
}
