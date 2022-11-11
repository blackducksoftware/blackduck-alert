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
import com.synopsys.integration.alert.common.rest.AlertRestConstants;

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

    //TODO: This is used by the old field model action services. In 8.0.0, this method can be removed.
    public AzureBoardsProperties createAzureBoardsProperties(String organizationName, String clientId, String clientSecret) throws AlertConfigurationException {
        AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModelSaved = azureBoardsGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .orElseThrow(() -> new AlertConfigurationException("Missing Azure Boards global configuration"));
        if (organizationName == null) {
            organizationName = azureBoardsGlobalConfigModelSaved.getOrganizationName();
        }
        if (clientId == null) {
            clientId = azureBoardsGlobalConfigModelSaved.getAppId().orElse("");
        }
        if (clientSecret == null) {
            clientSecret = azureBoardsGlobalConfigModelSaved.getClientSecret().orElse("");
        }

        AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel = new AzureBoardsGlobalConfigModel(
            azureBoardsGlobalConfigModelSaved.getId(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            organizationName,
            clientId,
            clientSecret
        );
        return AzureBoardsProperties.fromGlobalConfigurationModel(
            alertOAuthCredentialDataStoreFactory,
            azureRedirectUrlCreator.createOAuthRedirectUri(),
            azureBoardsGlobalConfigModel
        );
    }

}
