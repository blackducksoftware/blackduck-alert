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

import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsChannelKey;
import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUtil;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsProperties;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.web.common.BaseController;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.project.AzureProjectService;
import com.synopsys.integration.azure.boards.common.service.project.TeamProjectReferenceResponseModel;

@RestController
@RequestMapping(AzureOAuthCallbackController.AZURE_OAUTH_CALLBACK_PATH)
public class AzureOAuthCallbackController {
    public static final String AZURE_OAUTH_CALLBACK_PATH = BaseController.OAUTH_CALLBACK_PATH + "/azure";
    private final Logger logger = LoggerFactory.getLogger(AzureOAuthCallbackController.class);
    private final ResponseFactory responseFactory;
    private final Gson gson;
    private final AzureBoardsChannelKey azureBoardsChannelKey;
    private final AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory;
    private final ProxyManager proxyManager;
    private final ConfigurationAccessor configurationAccessor;
    private final AzureRedirectUtil azureRedirectUtil;
    private final OAuthRequestValidator oAuthRequestValidator;
    private final AuthorizationManager authorizationManager;

    @Autowired
    public AzureOAuthCallbackController(ResponseFactory responseFactory, Gson gson, AzureBoardsChannelKey azureBoardsChannelKey,
        AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory, ProxyManager proxyManager, ConfigurationAccessor configurationAccessor,
        AzureRedirectUtil azureRedirectUtil, OAuthRequestValidator oAuthRequestValidator, AuthorizationManager authorizationManager) {
        this.responseFactory = responseFactory;
        this.gson = gson;
        this.azureBoardsChannelKey = azureBoardsChannelKey;
        this.azureBoardsCredentialDataStoreFactory = azureBoardsCredentialDataStoreFactory;
        this.proxyManager = proxyManager;
        this.configurationAccessor = configurationAccessor;
        this.azureRedirectUtil = azureRedirectUtil;
        this.oAuthRequestValidator = oAuthRequestValidator;
        this.authorizationManager = authorizationManager;
    }

    @GetMapping
    public ResponseEntity<String> oauthCallback(HttpServletRequest request) {
        logger.debug("Azure OAuth callback method called");
        if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL.name(), azureBoardsChannelKey.getUniversalKey())) {
            logger.debug("Azure OAuth callback user does not have permission to call the controller.");
            return responseFactory.createForbiddenResponse();
        }

        String state = request.getParameter("state");
        try {
            String requestURI = request.getRequestURI();
            String requestQueryString = request.getQueryString();
            logger.debug("Request URI {}?{}", requestURI, requestQueryString);
            String authorizationCode = request.getParameter("code");
            if (!oAuthRequestValidator.hasRequestKey(state)) {
                logger.info("OAuth request {} not found.", state);
            } else {
                logger.info(createOAuthRequestLoggerMessage(state, "Processing..."));
                oAuthRequestValidator.removeAuthorizationRequest(state);
                FieldAccessor fieldAccessor = createFieldAccessor();
                if (fieldAccessor.getFields().isEmpty()) {
                    logger.error(createOAuthRequestLoggerMessage(state, "Azure oauth callback: Channel global configuration missing"));
                } else {
                    if (StringUtils.isBlank(authorizationCode)) {
                        logger.error(createOAuthRequestLoggerMessage(state, "Azure oauth callback: Authorization code isn't valid. Stop processing"));
                    } else {
                        String oAuthRedirectUri = azureRedirectUtil.createOAuthRedirectUri();
                        AzureBoardsProperties properties = AzureBoardsProperties.fromFieldAccessor(azureBoardsCredentialDataStoreFactory, oAuthRedirectUri, fieldAccessor);
                        testOAuthConnection(properties, authorizationCode, state);
                    }
                }
            }
        } catch (Exception ex) {
            // catch any exceptions so the redirect back to the UI happens and doesn't display the URL with the authorization code to the user.
            logger.error(createOAuthRequestLoggerMessage(state, "Azure OAuth callback error occurred"), ex);
        }
        // redirect back to the global channel configuration URL in the Alert UI.
        return responseFactory.createFoundRedirectResponse(azureRedirectUtil.createUIRedirectLocation());
    }

    private void testOAuthConnection(AzureBoardsProperties azureBoardsProperties, String authorizationCode, String oAuthRequestKey) {
        try {
            Proxy proxy = proxyManager.createProxy();
            String organizationName = azureBoardsProperties.getOrganizationName();
            // save initiate token requests with the authorization code.
            logger.info(createOAuthRequestLoggerMessage(oAuthRequestKey, "Testing with authorization code to save tokens."));
            testGetProjects(azureBoardsProperties.createAzureHttpService(proxy, gson, authorizationCode), organizationName, oAuthRequestKey);
            // load the oauth credentials from the store.

            logger.info(createOAuthRequestLoggerMessage(oAuthRequestKey, "Testing with store to read tokens."));
            testGetProjects(azureBoardsProperties.createAzureHttpService(proxy, gson), organizationName, oAuthRequestKey);
        } catch (AlertException ex) {
            logger.error(createOAuthRequestLoggerMessage(oAuthRequestKey, "Error in azure oauth validation test "), ex);
        }
    }

    // This method take a logger formatting string and appends a prefix for the OAuth authorization request in order to correlate the
    // authorization requests from the custom endpoint and the callback controller for debugging potential customer issues.
    private String createOAuthRequestLoggerMessage(String oAuthRequestKey, String loggerMessageFormat) {
        return String.format("OAuth request %s: %s", oAuthRequestKey, loggerMessageFormat);
    }

    private void testGetProjects(AzureHttpService azureHttpService, String organizationName, String oAuthRequestKey) {
        try {
            AzureProjectService azureProjectService = new AzureProjectService(azureHttpService);
            AzureArrayResponseModel<TeamProjectReferenceResponseModel> projects = azureProjectService.getProjects(organizationName);
            Integer projectCount = projects.getCount();
            logger.info(createOAuthRequestLoggerMessage(oAuthRequestKey, "Azure Boards project count: {}"), projectCount);
        } catch (HttpServiceException ex) {
            logger.error(createOAuthRequestLoggerMessage(oAuthRequestKey, "Error in azure oauth get projects validation test "), ex);
        }
    }

    private FieldAccessor createFieldAccessor() {
        Map<String, ConfigurationFieldModel> fields = new HashMap<>();
        try {
            List<ConfigurationModel> azureChannelConfigs = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(azureBoardsChannelKey, ConfigContextEnum.GLOBAL);
            Optional<ConfigurationModel> configModel = azureChannelConfigs.stream()
                                                           .findFirst();

            configModel
                .map(ConfigurationModel::getCopyOfKeyToFieldMap)
                .ifPresent(fields::putAll);
        } catch (AlertDatabaseConstraintException ex) {
            logger.error("Error reading Azure Channel configuration", ex);
        }
        return new FieldAccessor(fields);
    }
}
