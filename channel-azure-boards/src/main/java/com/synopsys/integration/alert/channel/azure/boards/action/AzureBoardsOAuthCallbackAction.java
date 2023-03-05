package com.synopsys.integration.alert.channel.azure.boards.action;

import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.oauth.AlertOAuthCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUrlCreator;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.azure.boards.common.http.AzureApiVersionAppender;
import com.synopsys.integration.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.project.AzureProjectService;
import com.synopsys.integration.azure.boards.common.service.project.TeamProjectReferenceResponseModel;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class AzureBoardsOAuthCallbackAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AlertOAuthCredentialDataStoreFactory alertOAuthCredentialDataStoreFactory;
    private final AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private final AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;
    private final Gson gson;
    private final AuthorizationManager authorizationManager;
    private final ProxyManager proxyManager;
    private final OAuthRequestValidator oAuthRequestValidator;
    private final AzureRedirectUrlCreator azureRedirectUrlCreator;

    @Autowired
    public AzureBoardsOAuthCallbackAction(
        AlertOAuthCredentialDataStoreFactory alertOAuthCredentialDataStoreFactory,
        AzureBoardsPropertiesFactory azureBoardsPropertiesFactory,
        AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor,
        Gson gson,
        AuthorizationManager authorizationManager,
        ProxyManager proxyManager,
        OAuthRequestValidator oAuthRequestValidator,
        AzureRedirectUrlCreator azureRedirectUrlCreator
    ) {
        this.alertOAuthCredentialDataStoreFactory = alertOAuthCredentialDataStoreFactory;
        this.azureBoardsPropertiesFactory = azureBoardsPropertiesFactory;
        this.azureBoardsGlobalConfigAccessor = azureBoardsGlobalConfigAccessor;
        this.gson = gson;
        this.authorizationManager = authorizationManager;
        this.proxyManager = proxyManager;
        this.oAuthRequestValidator = oAuthRequestValidator;
        this.azureRedirectUrlCreator = azureRedirectUrlCreator;
    }

    public ActionResponse<String> handleCallback(HttpServletRequest request) {
        logger.debug("Azure OAuth callback method called");
        if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL, ChannelKeys.AZURE_BOARDS)) {
            logger.debug("Azure OAuth callback user does not have permission to call the controller.");
            return ActionResponse.createForbiddenResponse();
        }

        String state = request.getParameter("state");
        UUID oAuthRequestId;
        try {
            oAuthRequestId = UUID.fromString(state);
        } catch (IllegalArgumentException ex) {
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, "Could not read UUID from callback request.");
        }
        try {
            String requestURI = request.getRequestURI();
            String requestQueryString = request.getQueryString();
            logger.debug("Request URI {}?{}", requestURI, requestQueryString);
            String authorizationCode = request.getParameter("code");
            if (!oAuthRequestValidator.hasRequestKey(oAuthRequestId)) {
                logger.info("OAuth request with id {}: not found.", oAuthRequestId);
            } else {
                logger.info("OAuth request with id {}: Processing...", oAuthRequestId);
                UUID azureGlobalConfigId = oAuthRequestValidator.getConfigurationIdFromRequest(oAuthRequestId);
                oAuthRequestValidator.removeAuthorizationRequest(oAuthRequestId);

                Optional<AzureBoardsGlobalConfigModel> savedConfigModelOptional = azureBoardsGlobalConfigAccessor.getConfiguration(azureGlobalConfigId);
                if (savedConfigModelOptional.isEmpty()) {
                    logger.error("OAuth request with id {}: Azure oauth callback: Channel global configuration missing", oAuthRequestId);
                } else {
                    AzureBoardsGlobalConfigModel savedConfigModel = savedConfigModelOptional.get();
                    if (StringUtils.isBlank(authorizationCode)) {
                        logger.error("OAuth request with id {}: Azure oauth callback: Authorization code isn't valid. Stop processing", oAuthRequestId);
                    } else {
                        String oAuthRedirectUri = azureRedirectUrlCreator.createOAuthRedirectUri();
                        AzureBoardsProperties properties = azureBoardsPropertiesFactory.fromGlobalConfigurationModel(
                            alertOAuthCredentialDataStoreFactory,
                            oAuthRedirectUri,
                            savedConfigModel
                        );
                        testOAuthConnection(properties, authorizationCode, oAuthRequestId);
                        return new ActionResponse<>(HttpStatus.OK, "Redirecting...", azureRedirectUrlCreator.createUIRedirectLocationForConfiguration(savedConfigModel.getId()));
                    }
                }
            }
        } catch (Exception ex) {
            // catch any exceptions so the redirect back to the UI happens and doesn't display the URL with the authorization code to the user.
            logger.error("OAuth request with id {}: Azure OAuth callback error occurred", oAuthRequestId, ex);
        }
        return new ActionResponse<>(HttpStatus.OK, "Redirecting...", azureRedirectUrlCreator.createUIRedirectLocation());
    }

    private void testOAuthConnection(AzureBoardsProperties azureBoardsProperties, String authorizationCode, UUID oAuthRequestId) {
        try {
            ProxyInfo proxyInfo = proxyManager.createProxyInfoForHost(AzureHttpRequestCreatorFactory.DEFAULT_BASE_URL);
            String organizationName = azureBoardsProperties.getOrganizationName();
            // save initiate token requests with the authorization code.
            logger.info("OAuth request with id {}: Testing with authorization code to save tokens.", oAuthRequestId);
            testGetProjects(azureBoardsProperties.createAzureHttpService(proxyInfo, gson, authorizationCode), organizationName, oAuthRequestId);
            // load the oauth credentials from the store.

            logger.info("OAuth request with id {}: Testing with store to read tokens.", oAuthRequestId);
            testGetProjects(azureBoardsProperties.createAzureHttpService(proxyInfo, gson), organizationName, oAuthRequestId);
        } catch (AlertException ex) {
            logger.error("OAuth request with id {}: Error in azure oauth validation test ", oAuthRequestId, ex);
            logger.error("Caused by: ", ex);
        }
    }

    private void testGetProjects(AzureHttpService azureHttpService, String organizationName, UUID oAuthRequestKey) {
        try {
            AzureProjectService azureProjectService = new AzureProjectService(azureHttpService, new AzureApiVersionAppender());
            AzureArrayResponseModel<TeamProjectReferenceResponseModel> projects = azureProjectService.getProjects(organizationName);
            Integer projectCount = projects.getCount();
            logger.info("OAuth request with id {}: Azure Boards project count: {}", oAuthRequestKey, projectCount);
        } catch (HttpServiceException ex) {
            logger.error("OAuth request with id {}: Error in azure oauth get projects validation test ", oAuthRequestKey, ex);
        }
    }

}
