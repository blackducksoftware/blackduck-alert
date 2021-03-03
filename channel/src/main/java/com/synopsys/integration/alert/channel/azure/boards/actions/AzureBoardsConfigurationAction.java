/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

@Component
public class AzureBoardsConfigurationAction extends ConfigurationAction {
    @Autowired
    protected AzureBoardsConfigurationAction(AzureBoardsDistributionTestAction azureBoardsDistributionTestAction, AzureBoardsGlobalTestAction azureBoardsGlobalTestAction,
        AzureBoardsGlobalApiAction azureBoardsGlobalApiAction) {
        super(ChannelKey.AZURE_BOARDS);
        addGlobalTestAction(azureBoardsGlobalTestAction);
        addGlobalApiAction(azureBoardsGlobalApiAction);
        addDistributionTestAction(azureBoardsDistributionTestAction);
    }

}
