package com.blackduck.integration.alert.channel.azure.boards.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.common.action.ConfigurationAction;

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
