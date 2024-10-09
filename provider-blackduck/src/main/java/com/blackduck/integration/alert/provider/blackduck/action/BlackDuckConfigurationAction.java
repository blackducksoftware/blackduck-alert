/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.common.action.ConfigurationAction;

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
