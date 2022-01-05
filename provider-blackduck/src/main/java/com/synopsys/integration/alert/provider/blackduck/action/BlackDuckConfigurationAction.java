/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;

@Component
public class BlackDuckConfigurationAction extends ConfigurationAction {
    @Autowired
    protected BlackDuckConfigurationAction(
        BlackDuckGlobalApiAction blackDuckGlobalApiAction,
        BlackDuckGlobalFieldModelTestAction blackDuckGlobalTestAction,
        BlackDuckProviderKey blackDuckProviderKey,
        BlackDuckDistributionFieldModelTestAction blackDuckDistributionTestAction
    ) {
        super(blackDuckProviderKey);
        addGlobalApiAction(blackDuckGlobalApiAction);
        addGlobalTestAction(blackDuckGlobalTestAction);
        addDistributionTestAction(blackDuckDistributionTestAction);
    }

}
