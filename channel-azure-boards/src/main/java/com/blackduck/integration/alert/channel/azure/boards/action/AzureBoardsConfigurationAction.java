/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.action.ConfigurationAction;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;

/**
 * @deprecated Global configuration actions for Jira Server are now handled through AzureBoardsGlobalCrudActions
 */
@Component
@Deprecated(forRemoval = true)
public class AzureBoardsConfigurationAction extends ConfigurationAction {
    @Autowired
    protected AzureBoardsConfigurationAction(AzureBoardsGlobalFieldModelTestAction azureBoardsGlobalTestAction, AzureBoardsGlobalApiAction azureBoardsGlobalApiAction) {
        super(ChannelKeys.AZURE_BOARDS);
        addGlobalTestAction(azureBoardsGlobalTestAction);
        addGlobalApiAction(azureBoardsGlobalApiAction);
    }

}
