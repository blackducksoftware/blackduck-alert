package com.synopsys.integration.alert.channel.azure.boards.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsChannelKey;
import com.synopsys.integration.alert.common.action.ConfigurationAction;

@Component
public class AzureBoardsConfigurationAction extends ConfigurationAction {
    @Autowired
    protected AzureBoardsConfigurationAction(AzureBoardsChannelKey descriptorKey, AzureBoardsDistributionTestAction azureBoardsDistributionTestAction, AzureBoardsGlobalTestAction azureBoardsGlobalTestAction) {
        super(descriptorKey);
        addGlobalTestAction(azureBoardsGlobalTestAction);
        addDistributionTestAction(azureBoardsDistributionTestAction);
    }
}
