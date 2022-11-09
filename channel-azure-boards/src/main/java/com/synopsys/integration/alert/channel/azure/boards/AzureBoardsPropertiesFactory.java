package com.synopsys.integration.alert.channel.azure.boards;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.oauth.AlertOAuthCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;

@Component
public class AzureBoardsPropertiesFactory {
    private final AlertOAuthCredentialDataStoreFactory alertOAuthCredentialDataStoreFactory;
    private final AzureRedirectUrlCreator azureRedirectUrlCreator;
    private final AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;
    private final JobAccessor jobAccessor;

    @Autowired
    public AzureBoardsPropertiesFactory(
        AlertOAuthCredentialDataStoreFactory alertOAuthCredentialDataStoreFactory,
        AzureRedirectUrlCreator azureRedirectUrlCreator,
        AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor,
        JobAccessor jobAccessor
    ) {
        this.alertOAuthCredentialDataStoreFactory = alertOAuthCredentialDataStoreFactory;
        this.azureRedirectUrlCreator = azureRedirectUrlCreator;
        this.azureBoardsGlobalConfigAccessor = azureBoardsGlobalConfigAccessor;
        this.jobAccessor = jobAccessor;
    }

    public AzureBoardsProperties createAzureBoardsProperties(UUID configurationId) throws AlertConfigurationException {
        AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel = azureBoardsGlobalConfigAccessor.getConfiguration(configurationId)
            .orElseThrow(() -> new AlertConfigurationException("Missing Azure Boards global configuration"));
        return AzureBoardsProperties.fromGlobalConfigurationModel(
            alertOAuthCredentialDataStoreFactory,
            azureRedirectUrlCreator.createOAuthRedirectUri(),
            azureBoardsGlobalConfigModel
        );
    }

    public AzureBoardsProperties createAzureBoardsPropertiesWithJobId(UUID azureBoardsJobId) throws AlertConfigurationException {
        DistributionJobModel azureBoardsDistributionJobConfiguration = jobAccessor.getJobById(azureBoardsJobId)
            .orElseThrow(() -> new AlertConfigurationException("Missing Azure Boards distribution configuration"));
        return createAzureBoardsProperties(azureBoardsDistributionJobConfiguration.getChannelGlobalConfigId());
    }

}
