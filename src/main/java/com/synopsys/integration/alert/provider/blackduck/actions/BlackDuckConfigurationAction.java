package com.synopsys.integration.alert.provider.blackduck.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class BlackDuckConfigurationAction extends ConfigurationAction {

    @Autowired
    protected BlackDuckConfigurationAction(final BlackDuckGlobalApiAction blackDuckGlobalApiAction, final BlackDuckGlobalTestAction blackDuckGlobalTestAction, final BlackDuckDistributionTestAction blackDuckDistributionTestAction) {
        super(BlackDuckProvider.COMPONENT_NAME);
        addGlobalApiAction(blackDuckGlobalApiAction);
        addGlobalTestAction(blackDuckGlobalTestAction);
        addDistributionTestAction(blackDuckDistributionTestAction);
    }
}
