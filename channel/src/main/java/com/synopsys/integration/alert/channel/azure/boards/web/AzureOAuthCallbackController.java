/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUtil;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsProperties;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.project.AzureProjectService;
import com.synopsys.integration.azure.boards.common.service.project.TeamProjectReferenceResponseModel;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@RestController
@RequestMapping(AzureOAuthCallbackController.AZURE_OAUTH_CALLBACK_PATH)
public class AzureOAuthCallbackController {
    public static final String AZURE = "azure";
    public static final String AZURE_OAUTH_CALLBACK_PATH = AlertRestConstants.OAUTH_CALLBACK_PATH + "/" + AZURE;
    private final Logger logger = LoggerFactory.getLogger(AzureOAuthCallbackController.class);
    private final ResponseFactory responseFactory;
    private final Gson gson;
    private final AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory;
    private final ProxyManager proxyManager;
    private final ConfigurationAccessor configurationAccessor;
    private final AzureRedirectUtil azureRedirectUtil;
    private final OAuthRequestValidator oAuthRequestValidator;
    private final AuthorizationManager authorizationManager;

    @Autowired
    public AzureOAuthCallbackController(ResponseFactory responseFactory, Gson gson, AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory, ProxyManager proxyManager, ConfigurationAccessor configurationAccessor,
        AzureRedirectUtil azureRedirectUtil, OAuthRequestValidator oAuthRequestValidator, AuthorizationManager authorizationManager) {
        this.responseFactory = responseFactory;
        this.gson = gson;
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
        if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL.name(), ChannelKeys.AZURE_BOARDS.getUniversalKey())) {
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
                logger.info(createOAuthRequestLoggerMessage(state, "not found."));
            } else {
                logger.info(createOAuthRequestLoggerMessage(state, "Processing..."));
                oAuthRequestValidator.removeAuthorizationRequest(state);
                FieldUtility fieldUtility = createFieldAccessor();
                if (fieldUtility.getFields().isEmpty()) {
                    logger.error(createOAuthRequestLoggerMessage(state, "Azure oauth callback: Channel global configuration missing"));
                } else {
                    if (StringUtils.isBlank(authorizationCode)) {
                        logger.error(createOAuthRequestLoggerMessage(state, "Azure oauth callback: Authorization code isn't valid. Stop processing"));
                    } else {
                        String oAuthRedirectUri = azureRedirectUtil.createOAuthRedirectUri();
                        AzureBoardsProperties properties = AzureBoardsProperties.fromFieldAccessor(azureBoardsCredentialDataStoreFactory, oAuthRedirectUri, fieldUtility);
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
            ProxyInfo proxyInfo = proxyManager.createProxyInfo();
            String organizationName = azureBoardsProperties.getOrganizationName();
            // save initiate token requests with the authorization code.
            logger.info(createOAuthRequestLoggerMessage(oAuthRequestKey, "Testing with authorization code to save tokens."));
            testGetProjects(azureBoardsProperties.createAzureHttpService(proxyInfo, gson, authorizationCode), organizationName, oAuthRequestKey);
            // load the oauth credentials from the store.

            logger.info(createOAuthRequestLoggerMessage(oAuthRequestKey, "Testing with store to read tokens."));
            testGetProjects(azureBoardsProperties.createAzureHttpService(proxyInfo, gson), organizationName, oAuthRequestKey);
        } catch (AlertException ex) {
            logger.error(createOAuthRequestLoggerMessage(oAuthRequestKey, "Error in azure oauth validation test "), ex);
        }
    }

    // This method take a logger formatting string and appends a prefix for the OAuth authorization request in order to correlate the
    // authorization requests from the custom endpoint and the callback controller for debugging potential customer issues.
    private String createOAuthRequestLoggerMessage(String oAuthRequestKey, String loggerMessageFormat) {
        String requestKey = StringUtils.isNotBlank(oAuthRequestKey) ? oAuthRequestKey : "<unknown value>";
        return String.format("OAuth request %s: %s", requestKey, loggerMessageFormat);
    }

    private void testGetProjects(AzureHttpService azureHttpService, String organizationName, String oAuthRequestKey) {
        try {
            AzureProjectService azureProjectService = new AzureProjectService(azureHttpService, new AzureApiVersionAppender());
            AzureArrayResponseModel<TeamProjectReferenceResponseModel> projects = azureProjectService.getProjects(organizationName);
            Integer projectCount = projects.getCount();
            logger.info(createOAuthRequestLoggerMessage(oAuthRequestKey, "Azure Boards project count: {}"), projectCount);
        } catch (HttpServiceException ex) {
            logger.error(createOAuthRequestLoggerMessage(oAuthRequestKey, "Error in azure oauth get projects validation test "), ex);
        }
    }

    private FieldUtility createFieldAccessor() {
        Map<String, ConfigurationFieldModel> fields = new HashMap<>();
        List<ConfigurationModel> azureChannelConfigs = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(ChannelKeys.AZURE_BOARDS, ConfigContextEnum.GLOBAL);
        azureChannelConfigs.stream()
            .findFirst()
            .map(ConfigurationModel::getCopyOfKeyToFieldMap)
            .ifPresent(fields::putAll);
        return new FieldUtility(fields);
    }

}
