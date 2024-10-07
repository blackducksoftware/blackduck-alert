/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.action;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.common.action.FieldModelTestAction;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.alert.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.synopsys.integration.alert.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.alert.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.alert.azure.boards.common.service.project.AzureProjectService;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.proxy.ProxyInfo;

/**
 * @deprecated Replaced by AzureBoardsGlobalTestAction. To be removed in 8.0.0.
 */
@Component
@Deprecated(forRemoval = true)
public class AzureBoardsGlobalFieldModelTestAction extends FieldModelTestAction {
    public static final Logger logger = LoggerFactory.getLogger(AzureBoardsGlobalFieldModelTestAction.class);

    private final Gson gson;
    private final AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private final ProxyManager proxyManager;

    @Autowired
    public AzureBoardsGlobalFieldModelTestAction(
        Gson gson,
        AzureBoardsPropertiesFactory azureBoardsPropertiesFactory, ProxyManager proxyManager
    ) {
        this.gson = gson;
        this.azureBoardsPropertiesFactory = azureBoardsPropertiesFactory;
        this.proxyManager = proxyManager;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        try {
            Optional<ConfigurationFieldModel> configurationFieldModel = registeredFieldValues.getField(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME);
            String organizationName = configurationFieldModel.flatMap(ConfigurationFieldModel::getFieldValue).orElse(null);

            AzureBoardsProperties azureBoardsProperties = createAzureBoardsProperties(registeredFieldValues);
            AzureHttpService azureHttpService = createAzureHttpService(azureBoardsProperties);
            AzureProjectService azureProjectService = new AzureProjectService(azureHttpService, new AzureApiVersionAppender());
            azureProjectService.getProjects(organizationName);
            return new MessageResult("Successfully connected to Azure instance.");
        } catch (HttpServiceException ex) {
            logger.error("Global Test Action failed testing Azure Boards connection.", ex);
            throw (ex);
        }
    }

    private AzureHttpService createAzureHttpService(AzureBoardsProperties azureBoardsProperties) throws IntegrationException {
        ProxyInfo proxy = proxyManager.createProxyInfoForHost(AzureHttpRequestCreatorFactory.DEFAULT_BASE_URL);
        return azureBoardsProperties.createAzureHttpService(proxy, gson);
    }

    private AzureBoardsProperties createAzureBoardsProperties(FieldUtility fieldUtility) throws AlertConfigurationException {
        String organizationName = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME);
        String clientId = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_CLIENT_ID);
        String clientSecret = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_CLIENT_SECRET);
        return azureBoardsPropertiesFactory.createAzureBoardsProperties(organizationName, clientId, clientSecret);
    }

}
