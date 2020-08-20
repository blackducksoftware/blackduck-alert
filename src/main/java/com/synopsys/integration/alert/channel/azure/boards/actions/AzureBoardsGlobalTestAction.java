package com.synopsys.integration.alert.channel.azure.boards.actions;

import java.net.Proxy;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUtil;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsProperties;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.project.AzureProjectService;
import com.synopsys.integration.azure.boards.common.service.project.TeamProjectReferenceResponseModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class AzureBoardsGlobalTestAction extends TestAction {
    public static final Logger logger = LoggerFactory.getLogger(AzureBoardsGlobalTestAction.class);
    private final Gson gson;
    private final AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory;
    private final AzureRedirectUtil azureRedirectUtil;
    private final ProxyManager proxyManager;

    @Autowired
    public AzureBoardsGlobalTestAction(Gson gson, AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory, AzureRedirectUtil azureRedirectUtil, ProxyManager proxyManager) {
        this.gson = gson;
        this.azureBoardsCredentialDataStoreFactory = azureBoardsCredentialDataStoreFactory;
        this.azureRedirectUtil = azureRedirectUtil;
        this.proxyManager = proxyManager;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldAccessor registeredFieldValues) throws IntegrationException {
        try {
            Optional<ConfigurationFieldModel> configurationFieldModel = registeredFieldValues.getField(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME);
            String organizationName = configurationFieldModel.map(model -> model.getFieldValue().get()).orElse(null);

            AzureBoardsProperties azureBoardsProperties = AzureBoardsProperties.fromFieldAccessor(azureBoardsCredentialDataStoreFactory, azureRedirectUtil.createOAuthRedirectUri(), registeredFieldValues);
            AzureHttpService azureHttpService = createAzureHttpService(azureBoardsProperties);
            AzureProjectService azureProjectService = new AzureProjectService(azureHttpService);
            //TODO may not need to save this to projects
            AzureArrayResponseModel<TeamProjectReferenceResponseModel> projects = azureProjectService.getProjects(organizationName);
            //Integer projectCount = projects.getCount();
            //logger.info("Azure Boards project count: {}", projectCount);
            return new MessageResult("Successfully connected to Azure instance.");
        } catch (HttpServiceException ex) {
            //TODO find out the fieldName
            AlertFieldStatus fieldStatus = AlertFieldStatus.error("test", ex.getMessage());
            return new MessageResult("An error occured during test.", List.of(fieldStatus));
        }

    }

    private AzureHttpService createAzureHttpService(AzureBoardsProperties azureBoardsProperties) throws IntegrationException {
        Proxy proxy = proxyManager.createProxy();
        return azureBoardsProperties.createAzureHttpService(proxy, gson);
    }
}
