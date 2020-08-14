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
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.project.AzureProjectService;
import com.synopsys.integration.azure.boards.common.service.project.TeamProjectReferenceResponseModel;

@RestController
@RequestMapping(AzureOauthCallbackController.AZURE_OAUTH_CALLBACK_PATH)
public class AzureOauthCallbackController {
    public static final String AZURE_OAUTH_CALLBACK_PATH = BaseController.OAUTH_CALLBACK_PATH + "/azure";
    private Logger logger = LoggerFactory.getLogger(AzureOauthCallbackController.class);
    private ResponseFactory responseFactory;
    private final Gson gson;
    private final AzureBoardsChannelKey azureBoardsChannelKey;
    private final AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory;
    private final ProxyManager proxyManager;
    private final ConfigurationAccessor configurationAccessor;
    private final AzureRedirectUtil azureRedirectUtil;

    @Autowired
    public AzureOauthCallbackController(ResponseFactory responseFactory, Gson gson, AzureBoardsChannelKey azureBoardsChannelKey,
        AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory, ProxyManager proxyManager, ConfigurationAccessor configurationAccessor,
        AzureRedirectUtil azureRedirectUtil) {
        this.responseFactory = responseFactory;
        this.gson = gson;
        this.azureBoardsChannelKey = azureBoardsChannelKey;
        this.azureBoardsCredentialDataStoreFactory = azureBoardsCredentialDataStoreFactory;
        this.proxyManager = proxyManager;
        this.configurationAccessor = configurationAccessor;
        this.azureRedirectUtil = azureRedirectUtil;
    }

    @GetMapping
    public ResponseEntity<String> oauthCallback(HttpServletRequest request) {
        logger.debug("Azure OAuth callback method called");
        try {
            String requestURI = request.getRequestURI();
            String requestQueryString = request.getQueryString();
            logger.debug("Request URI {}?{}", requestURI, requestQueryString);
            String authorizationCode = request.getParameter("code");
            String state = request.getParameter("state");
            FieldAccessor fieldAccessor = createFieldAccessor();
            if (fieldAccessor.getFields().isEmpty()) {
                logger.error("Azure oauth callback: Channel global configuration missing");
            } else {
                if (StringUtils.isBlank(authorizationCode)) {
                    logger.error("Azure oauth callback: Authorization code isn't valid. Stop processing");
                } else {
                    String oAuthRedirectUri = azureRedirectUtil.createOAuthRedirectUri();
                    AzureBoardsProperties properties = AzureBoardsProperties.fromFieldAccessor(azureBoardsCredentialDataStoreFactory, oAuthRedirectUri, fieldAccessor);
                    // TODO lookup authorization request and redirect back to the Alert Azure global channel page.
                    testOAuthConnection(properties, authorizationCode);
                }
            }
        } catch (Exception ex) {
            // catch any exceptions so the redirect back to the UI happens and doesn't display the URL with the authorization code to the user.
            logger.error("Azure OAuth callback error occurred", ex);
        }
        // redirect back to the global channel configuration URL in the Alert UI.
        return responseFactory.createFoundRedirectResponse(azureRedirectUtil.createUIRedirectLocation());
    }

    private void testOAuthConnection(AzureBoardsProperties azureBoardsProperties, String authorizationCode) {
        try {
            Proxy proxy = proxyManager.createProxy();
            // save initiate token requests with the authorization code.
            AzureHttpService azureService = azureBoardsProperties.createAzureHttpService(proxy, gson, authorizationCode);
            // load the oauth credentials from the store.
            azureService = azureBoardsProperties.createAzureHttpService(proxy, gson);

            AzureProjectService azureProjectService = new AzureProjectService(azureService);

            logger.info("Azure Service created with the oauth parameters.");
            AzureArrayResponseModel<TeamProjectReferenceResponseModel> projects = azureProjectService.getProjects(azureBoardsProperties.getOrganizationName());
            Integer projectCount = projects.getCount();
            logger.info("Azure Boards project count: {}", projectCount);
        } catch (AlertException | HttpServiceException ex) {
            logger.error("Error in azure oauth validation test ", ex);
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
