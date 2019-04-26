package com.synopsys.integration.alert.component.scheduling.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.component.settings.actions.SettingsGlobalApiAction;

@Component
public class SchedulingConfigurationAction extends ConfigurationAction {

    @Autowired
    protected SchedulingConfigurationAction(final SettingsGlobalApiAction settingsGlobalApiAction) {
        super(SchedulingDescriptor.SCHEDULING_COMPONENT);
        addGlobalApiAction(settingsGlobalApiAction);
    }
}
