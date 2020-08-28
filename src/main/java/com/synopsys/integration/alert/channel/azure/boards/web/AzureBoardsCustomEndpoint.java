/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.azure.boards.web;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsChannelKey;
import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUtil;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsProperties;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthCustomEndpoint;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointResponse;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.web.api.config.ConfigActions;
import com.synopsys.integration.azure.boards.common.http.AzureHttpServiceFactory;
import com.synopsys.integration.azure.boards.common.oauth.AzureOAuthScopes;

@Component
public class AzureBoardsCustomEndpoint extends OAuthCustomEndpoint {
    private final Logger logger = LoggerFactory.getLogger(AzureBoardsCustomEndpoint.class);

    private final AlertProperties alertProperties;
    private final ConfigurationAccessor configurationAccessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory;
    private final AzureRedirectUtil azureRedirectUtil;
    private final ProxyManager proxyManager;
    private final OAuthRequestValidator oAuthRequestValidator;
    private final ConfigActions configActions;
    private final AuthorizationManager authorizationManager;
    private final AzureBoardsChannelKey azureBoardsChannelKey;

    public AzureBoardsCustomEndpoint(CustomEndpointManager customEndpointManager, ResponseFactory responseFactory, Gson gson, AlertProperties alertProperties, ConfigurationAccessor configurationAccessor,
        ConfigurationFieldModelConverter modelConverter, AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory, AzureRedirectUtil azureRedirectUtil,
        ProxyManager proxyManager, OAuthRequestValidator oAuthRequestValidator, ConfigActions configActions, AuthorizationManager authorizationManager,
        AzureBoardsChannelKey azureBoardsChannelKey)
        throws AlertException {
        super(AzureBoardsDescriptor.KEY_OAUTH, customEndpointManager, responseFactory, gson);
        this.alertProperties = alertProperties;
        this.configurationAccessor = configurationAccessor;
        this.modelConverter = modelConverter;
        this.azureBoardsCredentialDataStoreFactory = azureBoardsCredentialDataStoreFactory;
        this.azureRedirectUtil = azureRedirectUtil;
        this.proxyManager = proxyManager;
        this.oAuthRequestValidator = oAuthRequestValidator;
        this.configActions = configActions;
        this.authorizationManager = authorizationManager;
        this.azureBoardsChannelKey = azureBoardsChannelKey;
    }

    @Override
    protected OAuthEndpointResponse createOAuthResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        try {
            if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL.name(), azureBoardsChannelKey.getUniversalKey())) {
                logger.debug("Azure OAuth callback user does not have permission to call the controller.");
                return new OAuthEndpointResponse(HttpStatus.FORBIDDEN.value(), false, "", ResponseFactory.UNAUTHORIZED_REQUEST_MESSAGE);
            }
            String requestKey = createRequestKey();
            // since we have only one OAuth channel now remove all other requests.
            // if we have more OAuth clients then the removeAllRequests will have to be removed from here.
            // beginning authentication process create the request id at the start.
            oAuthRequestValidator.removeAllRequests();
            oAuthRequestValidator.addAuthorizationRequest(requestKey);
            Optional<FieldModel> savedFieldModel = saveIfValid(fieldModel);
            if (!savedFieldModel.isPresent()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "");
            }
            FieldAccessor fieldAccessor = createFieldAccessor(savedFieldModel.get());
            Optional<String> clientId = fieldAccessor.getString(AzureBoardsDescriptor.KEY_CLIENT_ID);
            if (!clientId.isPresent()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "client id not found.");
            }
            Optional<String> alertServerUrl = alertProperties.getServerUrl();

            if (!alertServerUrl.isPresent()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Could not determine the alert server url for the callback.");
            }

            logger.info("OAuth authorization request created: {}", requestKey);
            String authUrl = createAuthURL(clientId.get(), requestKey);
            logger.debug("Authenticating Azure OAuth URL: " + authUrl);
            return new OAuthEndpointResponse(HttpStatus.OK.value(), isAuthenticated(fieldAccessor), authUrl, "Authenticating...");

        } catch (AlertFieldException ex) {
            logger.error("Error activating Azure Boards", ex);
            Set<String> errors = ex.getFieldErrors().stream()
                                     .filter(fieldStatus -> FieldStatusSeverity.ERROR == fieldStatus.getSeverity())
                                     .map(AlertFieldStatus::getFieldMessage)
                                     .collect(Collectors.toSet());
            String errorMessage = String.format(
                "The configuration is invalid. Please test the configuration. Details: %s", StringUtils.join(errors, ","));
            return createErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);

        } catch (Exception ex) {
            logger.error("Error activating Azure Boards", ex);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error activating azure oauth.");
        }
    }

    private OAuthEndpointResponse createErrorResponse(HttpStatus httpStatus, String errorMessage) {
        oAuthRequestValidator.removeAllRequests();
        return new OAuthEndpointResponse(httpStatus.value(), false, "", errorMessage);
    }

    private Optional<FieldModel> saveIfValid(FieldModel fieldModel) throws AlertException {
        if (StringUtils.isNotBlank(fieldModel.getId())) {
            if (authorizationManager.hasWritePermission(ConfigContextEnum.GLOBAL.name(), azureBoardsChannelKey.getUniversalKey())) {
                Long id = Long.parseLong(fieldModel.getId());
                return Optional.ofNullable(configActions.updateConfig(id, fieldModel));
            }
        } else {
            if (authorizationManager.hasCreatePermission(ConfigContextEnum.GLOBAL.name(), azureBoardsChannelKey.getUniversalKey())) {
                return Optional.ofNullable(configActions.saveConfig(fieldModel, azureBoardsChannelKey));
            }
        }
        return Optional.empty();
    }

    private FieldAccessor createFieldAccessor(FieldModel fieldModel) {
        Map<String, ConfigurationFieldModel> fields = new HashMap<>();
        try {
            fields.putAll(modelConverter.convertToConfigurationFieldModelMap(fieldModel));
            // check if a configuration exists because the client id is a sensitive field and won't have a value in the field model if updating.
            if (StringUtils.isNotBlank(fieldModel.getId())) {
                configurationAccessor.getConfigurationById(Long.valueOf(fieldModel.getId()))
                    .map(ConfigurationModel::getCopyOfKeyToFieldMap)
                    .ifPresent(fields::putAll);
            }
        } catch (AlertDatabaseConstraintException ex) {
            logger.error("Error creating field accessor for Azure authentication", ex);
        }
        return new FieldAccessor(fields);
    }

    private boolean isAuthenticated(FieldAccessor fieldAccessor) {
        AzureBoardsProperties properties = AzureBoardsProperties.fromFieldAccessor(azureBoardsCredentialDataStoreFactory, azureRedirectUtil.createOAuthRedirectUri(), fieldAccessor);
        return properties.hasOAuthCredentials(proxyManager.createProxyInfo());
    }

    private String createAuthURL(String clientId, String requestKey) {
        StringBuilder authUrlBuilder = new StringBuilder(300);
        authUrlBuilder.append(AzureHttpServiceFactory.DEFAULT_AUTHORIZATION_URL);
        authUrlBuilder.append(createQueryString(clientId, requestKey));
        return authUrlBuilder.toString();
    }

    private String createQueryString(String clientId, String requestKey) {
        List<String> scopes = List.of(AzureOAuthScopes.PROJECTS_READ.getScope(), AzureOAuthScopes.WORK_FULL.getScope());
        String authorizationUrl = azureRedirectUtil.createOAuthRedirectUri();
        StringBuilder queryBuilder = new StringBuilder(250);
        queryBuilder.append("&client_id=");
        queryBuilder.append(clientId);
        //TODO have an object that stores the request keys and purges them after some amount of time.
        queryBuilder.append("&state=");
        queryBuilder.append(requestKey);
        queryBuilder.append("&scope=");
        queryBuilder.append(URLEncoder.encode(StringUtils.join(scopes, " "), StandardCharsets.UTF_8));
        queryBuilder.append("&redirect_uri=");
        queryBuilder.append(URLEncoder.encode(authorizationUrl, StandardCharsets.UTF_8));
        return queryBuilder.toString();
    }

    private String createRequestKey() {
        UUID requestID = UUID.randomUUID();
        return String.format("%s-%s", "alert-oauth-request", requestID.toString());
    }
}
