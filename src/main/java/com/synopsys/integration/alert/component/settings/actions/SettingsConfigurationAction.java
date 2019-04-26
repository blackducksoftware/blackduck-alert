package com.synopsys.integration.alert.component.settings.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;

@Component
public class SettingsConfigurationAction extends ConfigurationAction {

    @Autowired
    protected SettingsConfigurationAction(final SettingsGlobalApiAction settingsGlobalApiAction) {
        super(SettingsDescriptor.SETTINGS_COMPONENT);
        addGlobalApiAction(settingsGlobalApiAction);
    }
}
