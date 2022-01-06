/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUrlCreator;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.action.api.ConfigResourceActions;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointResponse;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.AlertWebServerUrlManager;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.synopsys.integration.azure.boards.common.oauth.AzureOAuthScopes;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class AzureBoardsCustomFunctionAction extends CustomFunctionAction<OAuthEndpointResponse> {
    private final Logger logger = LoggerFactory.getLogger(AzureBoardsCustomFunctionAction.class);

    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final AzureBoardsGlobalConfigurationFieldModelValidator globalConfigurationValidator;
    private final AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory;
    private final AzureRedirectUrlCreator azureRedirectUrlCreator;
    private final ProxyManager proxyManager;
    private final OAuthRequestValidator oAuthRequestValidator;
    private final ConfigResourceActions configActions;
    private final AlertWebServerUrlManager alertWebServerUrlManager;

    @Autowired
    public AzureBoardsCustomFunctionAction(
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        ConfigurationFieldModelConverter modelConverter,
        AzureBoardsGlobalConfigurationFieldModelValidator globalConfigurationValidator,
        AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory,
        AzureRedirectUrlCreator azureRedirectUrlCreator,
        ProxyManager proxyManager,
        OAuthRequestValidator oAuthRequestValidator,
        ConfigResourceActions configActions,
        AuthorizationManager authorizationManager,
        AlertWebServerUrlManager alertWebServerUrlManager
    ) {
        super(authorizationManager);
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.modelConverter = modelConverter;
        this.globalConfigurationValidator = globalConfigurationValidator;
        this.azureBoardsCredentialDataStoreFactory = azureBoardsCredentialDataStoreFactory;
        this.azureRedirectUrlCreator = azureRedirectUrlCreator;
        this.proxyManager = proxyManager;
        this.oAuthRequestValidator = oAuthRequestValidator;
        this.configActions = configActions;
        this.alertWebServerUrlManager = alertWebServerUrlManager;
    }

    @Override
    public ActionResponse<OAuthEndpointResponse> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        try {
            Optional<FieldModel> savedFieldModel = saveIfValid(fieldModel);
            if (!savedFieldModel.isPresent()) {
                return createErrorResponse("The configuration is invalid. Please test the configuration.");
            }
            FieldUtility fieldUtility = createFieldAccessor(savedFieldModel.get());
            Optional<String> clientId = fieldUtility.getString(AzureBoardsDescriptor.KEY_CLIENT_ID);
            if (!clientId.isPresent()) {
                return createErrorResponse("App ID not found.");
            }

            Optional<String> alertServerUrl = alertWebServerUrlManager.getServerUrl();
            if (!alertServerUrl.isPresent()) {
                return createErrorResponse("Could not determine the alert server url for the callback.");
            }

            String requestKey = oAuthRequestValidator.generateRequestKey();
            // since we have only one OAuth channel now remove any other requests.
            // if we have more OAuth clients then the "remove requests" will have to be removed from here.
            // beginning authentication process create the request id at the start.
            oAuthRequestValidator.removeRequestsOlderThan5MinutesAgo();
            oAuthRequestValidator.addAuthorizationRequest(requestKey);

            logger.info("OAuth authorization request created: {}", requestKey);
            String authUrl = createAuthURL(clientId.get(), requestKey);
            logger.debug("Authenticating Azure OAuth URL: {}", authUrl);
            return new ActionResponse<>(HttpStatus.OK, new OAuthEndpointResponse(isAuthenticated(fieldUtility), authUrl, "Authenticating..."));
        } catch (Exception ex) {
            logger.error("Error activating Azure Boards", ex);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error activating azure oauth.");
        }
    }

    @Override
    protected Collection<AlertFieldStatus> validateRelatedFields(FieldModel fieldModel) {
        return globalConfigurationValidator.validate(fieldModel);
    }

    private ActionResponse<OAuthEndpointResponse> createErrorResponse(String errorMessage) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    private ActionResponse<OAuthEndpointResponse> createErrorResponse(HttpStatus httpStatus, String errorMessage) {
        oAuthRequestValidator.removeAllRequests();
        OAuthEndpointResponse oAuthEndpointResponse = new OAuthEndpointResponse(false, "", errorMessage);
        return new ActionResponse<>(httpStatus, errorMessage, oAuthEndpointResponse);
    }

    private Optional<FieldModel> saveIfValid(FieldModel fieldModel) {
        if (StringUtils.isNotBlank(fieldModel.getId())) {
            Long id = Long.parseLong(fieldModel.getId());
            ActionResponse<FieldModel> response = configActions.update(id, fieldModel);
            return response.getContent();
        } else {
            ActionResponse<FieldModel> response = configActions.create(fieldModel);
            return response.getContent();
        }
    }

    private FieldUtility createFieldAccessor(FieldModel fieldModel) {
        Map<String, ConfigurationFieldModel> fields = new HashMap<>(modelConverter.convertToConfigurationFieldModelMap(fieldModel));
        // check if a configuration exists because the client id is a sensitive field and won't have a value in the field model if updating.
        if (StringUtils.isNotBlank(fieldModel.getId())) {
            configurationModelConfigurationAccessor.getConfigurationById(Long.valueOf(fieldModel.getId()))
                .map(ConfigurationModel::getCopyOfKeyToFieldMap)
                .ifPresent(fields::putAll);
        }
        return new FieldUtility(fields);
    }

    private boolean isAuthenticated(FieldUtility fieldUtility) {
        AzureBoardsProperties properties = AzureBoardsProperties.fromFieldAccessor(azureBoardsCredentialDataStoreFactory, azureRedirectUrlCreator.createOAuthRedirectUri(), fieldUtility);
        ProxyInfo proxy = proxyManager.createProxyInfoForHost(AzureHttpRequestCreatorFactory.DEFAULT_BASE_URL);
        return properties.hasOAuthCredentials(proxy);
    }

    private String createAuthURL(String clientId, String requestKey) {
        StringBuilder authUrlBuilder = new StringBuilder(300);
        authUrlBuilder.append(AzureHttpRequestCreatorFactory.DEFAULT_AUTHORIZATION_URL);
        authUrlBuilder.append(createQueryString(clientId, requestKey));
        return authUrlBuilder.toString();
    }

    private String createQueryString(String clientId, String requestKey) {
        List<String> scopes = List.of(AzureOAuthScopes.PROJECTS_READ.getScope(), AzureOAuthScopes.WORK_FULL.getScope());
        String authorizationUrl = azureRedirectUrlCreator.createOAuthRedirectUri();
        StringBuilder queryBuilder = new StringBuilder(250);
        queryBuilder.append("&client_id=");
        queryBuilder.append(clientId);
        queryBuilder.append("&state=");
        queryBuilder.append(requestKey);
        queryBuilder.append("&scope=");
        queryBuilder.append(URLEncoder.encode(StringUtils.join(scopes, " "), StandardCharsets.UTF_8));
        queryBuilder.append("&redirect_uri=");
        queryBuilder.append(URLEncoder.encode(authorizationUrl, StandardCharsets.UTF_8));
        return queryBuilder.toString();
    }

}
