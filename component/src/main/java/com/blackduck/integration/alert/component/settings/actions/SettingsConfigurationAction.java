package com.blackduck.integration.alert.component.settings.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.action.ConfigurationAction;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptorKey;

/**
 * @deprecated This class is unused and part of the old Settings encryption & proxy REST API. It is set for removal in 8.0.0.
 */
@Component
@Deprecated(forRemoval = true)
public class SettingsConfigurationAction extends ConfigurationAction {
    @Autowired
    protected SettingsConfigurationAction(SettingsDescriptorKey settingsDescriptorKey, SettingsGlobalApiAction settingsGlobalApiAction) {
        super(settingsDescriptorKey);
        addGlobalApiAction(settingsGlobalApiAction);
    }

}
