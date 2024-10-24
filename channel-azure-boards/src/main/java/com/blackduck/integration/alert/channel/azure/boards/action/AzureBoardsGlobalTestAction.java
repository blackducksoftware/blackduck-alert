/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.action;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.oauth.AlertOAuthCredentialDataStoreFactory;
import com.blackduck.integration.alert.azure.boards.common.http.AzureApiVersionAppender;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpService;
import com.blackduck.integration.alert.azure.boards.common.service.project.AzureProjectService;
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.blackduck.integration.alert.channel.azure.boards.AzureRedirectUrlCreator;
import com.blackduck.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.action.ValidationActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.message.model.ConfigurationTestResult;
import com.blackduck.integration.alert.common.rest.api.ConfigurationTestHelper;
import com.blackduck.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;

@Component
public class AzureBoardsGlobalTestAction {
    private final AzureBoardsGlobalConfigurationValidator validator;
    private final AzureBoardsGlobalConfigAccessor configurationAccessor;
    private final AlertOAuthCredentialDataStoreFactory alertOAuthCredentialDataStoreFactory;
    private final AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private final AzureRedirectUrlCreator azureRedirectUrlCreator;
    private final ConfigurationValidationHelper validationHelper;
    private final ConfigurationTestHelper testHelper;
    private final Gson gson;
    private final ProxyManager proxyManager;

    @Autowired
    public AzureBoardsGlobalTestAction(
        AuthorizationManager authorizationManager,
        AzureBoardsGlobalConfigurationValidator validator,
        AzureBoardsGlobalConfigAccessor configurationAccessor,
        AlertOAuthCredentialDataStoreFactory alertOAuthCredentialDataStoreFactory,
        AzureBoardsPropertiesFactory azureBoardsPropertiesFactory,
        AzureRedirectUrlCreator azureRedirectUrlCreator,
        Gson gson,
        ProxyManager proxyManager
    ) {
        this.testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.AZURE_BOARDS);
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.AZURE_BOARDS);
        this.proxyManager = proxyManager;
        this.gson = gson;
        this.alertOAuthCredentialDataStoreFactory = alertOAuthCredentialDataStoreFactory;
        this.azureBoardsPropertiesFactory = azureBoardsPropertiesFactory;
        this.azureRedirectUrlCreator = azureRedirectUrlCreator;
        this.validator = validator;
        this.configurationAccessor = configurationAccessor;
    }

    public ActionResponse<ValidationResponseModel> testWithPermissionCheck(AzureBoardsGlobalConfigModel requestResource) {
        Supplier<ValidationActionResponse> validationSupplier = () -> validationHelper.validate(() -> validator.validate(requestResource, requestResource.getId()));
        return testHelper.test(validationSupplier, () -> testConfigModelContent(requestResource));
    }

    protected ConfigurationTestResult testConfigModelContent(AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel) {
        try {
            boolean isAppIdSet = BooleanUtils.toBoolean(azureBoardsGlobalConfigModel.getIsAppIdSet().orElse(Boolean.FALSE));
            boolean isClientSecretSet = BooleanUtils.toBoolean(azureBoardsGlobalConfigModel.getIsClientSecretSet().orElse(Boolean.FALSE));
            if (isAppIdSet || isClientSecretSet) {
                Optional<AzureBoardsGlobalConfigModel> accessorModel = configurationAccessor.getConfiguration(UUID.fromString(azureBoardsGlobalConfigModel.getId()));

                if (isAppIdSet) {
                    accessorModel.flatMap(AzureBoardsGlobalConfigModel::getAppId).ifPresent(azureBoardsGlobalConfigModel::setAppId);
                }
                if (isClientSecretSet) {
                    accessorModel.flatMap(AzureBoardsGlobalConfigModel::getClientSecret).ifPresent(azureBoardsGlobalConfigModel::setClientSecret);
                }
            }

            AzureBoardsProperties azureBoardsProperties = azureBoardsPropertiesFactory.fromGlobalConfigurationModel(
                alertOAuthCredentialDataStoreFactory,
                azureRedirectUrlCreator.createOAuthRedirectUri(),
                azureBoardsGlobalConfigModel
            );
            // Just make sure service creations and getting project succeeds
            AzureProjectService azureProjectService = createAzureProjectService(azureBoardsProperties);
            azureProjectService.getProjects(azureBoardsGlobalConfigModel.getOrganizationName());
        } catch (IntegrationException ex) {
            return ConfigurationTestResult.failure("Global Test Action failed testing Azure Boards connection." + ex.getMessage());
        }
        return ConfigurationTestResult.success("Successfully connected to Azure instance.");
    }

    protected AzureProjectService createAzureProjectService(AzureBoardsProperties azureBoardsProperties) throws IntegrationException {
        ProxyInfo proxy = proxyManager.createProxyInfoForHost(AzureHttpRequestCreatorFactory.DEFAULT_BASE_URL);
        AzureHttpService azureHttpService = azureBoardsProperties.createAzureHttpService(proxy, gson);
        return new AzureProjectService(azureHttpService, new AzureApiVersionAppender());
    }
}
