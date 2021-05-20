/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;

@Component
public class AzureBoardsPropertiesFactory {
    private final AzureBoardsChannelKey channelKey;
    private final AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory;
    private final AzureRedirectUrlCreator azureRedirectUrlCreator;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public AzureBoardsPropertiesFactory(AzureBoardsChannelKey channelKey, AzureBoardsCredentialDataStoreFactory credentialDataStoreFactory,
        AzureRedirectUrlCreator azureRedirectUrlCreator, ConfigurationAccessor configurationAccessor) {
        this.channelKey = channelKey;
        this.credentialDataStoreFactory = credentialDataStoreFactory;
        this.azureRedirectUrlCreator = azureRedirectUrlCreator;
        this.configurationAccessor = configurationAccessor;
    }

    public AzureBoardsProperties createAzureBoardsProperties() throws AlertConfigurationException {
        ConfigurationModel azureBoardsGlobalConfiguration = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(channelKey, ConfigContextEnum.GLOBAL)
                                                                .stream()
                                                                .findAny()
                                                                .orElseThrow(() -> new AlertConfigurationException("Missing Azure Boards global configuration"));
        return AzureBoardsProperties.fromGlobalConfig(credentialDataStoreFactory, azureRedirectUrlCreator.createOAuthRedirectUri(), azureBoardsGlobalConfiguration);
    }

}
