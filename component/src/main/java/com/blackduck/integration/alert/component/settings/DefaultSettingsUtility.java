package com.blackduck.integration.alert.component.settings;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.blackduck.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;

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
