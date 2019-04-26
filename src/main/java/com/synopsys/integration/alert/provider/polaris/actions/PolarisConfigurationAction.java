package com.synopsys.integration.alert.provider.polaris.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;

@Component
public class PolarisConfigurationAction extends ConfigurationAction {

    @Autowired
    public PolarisConfigurationAction(final PolarisGlobalApiAction polarisGlobalApiAction, final PolarisGlobalTestAction polarisGlobalTestAction) {
        super(PolarisProvider.COMPONENT_NAME);
        addGlobalApiAction(polarisGlobalApiAction);
        addGlobalTestAction(polarisGlobalTestAction);
    }
}
