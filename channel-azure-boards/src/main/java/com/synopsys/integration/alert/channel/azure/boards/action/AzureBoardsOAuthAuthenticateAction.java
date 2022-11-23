package com.synopsys.integration.alert.channel.azure.boards.action;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.oauth.AlertOAuthCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUrlCreator;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.AlertWebServerUrlManager;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.synopsys.integration.azure.boards.common.oauth.AzureOAuthScopes;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class AzureBoardsOAuthAuthenticateAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AuthorizationManager authorizationManager;
    private final AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;
    private final AzureBoardsGlobalCrudActions azureBoardsGlobalCrudActions;
    private final AlertOAuthCredentialDataStoreFactory alertOAuthCredentialDataStoreFactory;
    private final AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private final AzureBoardsGlobalValidationAction azureBoardsGlobalValidationAction;
    private final AzureRedirectUrlCreator azureRedirectUrlCreator;
    private final OAuthRequestValidator oAuthRequestValidator;
    private final AlertWebServerUrlManager alertWebServerUrlManager;
    private final ProxyManager proxyManager;

    @Autowired
    public AzureBoardsOAuthAuthenticateAction(
        AuthorizationManager authorizationManager,
        AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor,
        AzureBoardsGlobalCrudActions azureBoardsGlobalCrudActions,
        AlertOAuthCredentialDataStoreFactory alertOAuthCredentialDataStoreFactory,
        AzureBoardsPropertiesFactory azureBoardsPropertiesFactory,
        AzureBoardsGlobalValidationAction azureBoardsGlobalValidationAction,
        AzureRedirectUrlCreator azureRedirectUrlCreator,
        OAuthRequestValidator oAuthRequestValidator,
        AlertWebServerUrlManager alertWebServerUrlManager,
        ProxyManager proxyManager
    ) {
        this.authorizationManager = authorizationManager;
        this.azureBoardsGlobalConfigAccessor = azureBoardsGlobalConfigAccessor;
        this.azureBoardsGlobalCrudActions = azureBoardsGlobalCrudActions;
        this.alertOAuthCredentialDataStoreFactory = alertOAuthCredentialDataStoreFactory;
        this.azureBoardsPropertiesFactory = azureBoardsPropertiesFactory;
        this.azureRedirectUrlCreator = azureRedirectUrlCreator;
        this.azureBoardsGlobalValidationAction = azureBoardsGlobalValidationAction;
        this.oAuthRequestValidator = oAuthRequestValidator;
        this.alertWebServerUrlManager = alertWebServerUrlManager;
        this.proxyManager = proxyManager;
    }

    public ActionResponse<OAuthEndpointResponse> authenticate(AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel) {
        ActionResponse<ValidationResponseModel> validationResponse = azureBoardsGlobalValidationAction.validate(azureBoardsGlobalConfigModel);
        boolean validationHasErrors = validationResponse.getContent().map(ValidationResponseModel::hasErrors).orElse(false);
        if (validationHasErrors) {
            return createErrorResponse("The configuration is invalid. Please test the configuration.");
        }

        ActionResponse<AzureBoardsGlobalConfigModel> savedConfigModelResponse = createOrUpdateModel(azureBoardsGlobalConfigModel);
        if (savedConfigModelResponse.isError()) {
            return new ActionResponse<>(
                savedConfigModelResponse.getHttpStatus(),
                savedConfigModelResponse.getMessage().orElse("An error occurred saving the Azure Boards global configuration.")
            );
        }

        Optional<AzureBoardsGlobalConfigModel> configContentOptional = savedConfigModelResponse.getContent();
        if (configContentOptional.isEmpty()) {
            return createErrorResponse("Could not find the saved Azure Boards configuration.");
        }
        UUID savedAzureConfigId = UUID.fromString(configContentOptional.get().getId());

        Optional<AzureBoardsGlobalConfigModel> savedConfigModelOptional = azureBoardsGlobalConfigAccessor.getConfiguration(savedAzureConfigId);
        if (savedConfigModelOptional.isEmpty()) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "Could not find the saved Azure Boards configuration.");
        }
        AzureBoardsGlobalConfigModel savedConfigModel = savedConfigModelOptional.get();

        Optional<String> appIdOptional = savedConfigModel.getAppId();
        if (appIdOptional.isEmpty()) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "Could not find the Azure Boards App Id.");
        }
        String appId = appIdOptional.get();

        Optional<String> alertServerUrl = alertWebServerUrlManager.getServerUrl();
        if (alertServerUrl.isEmpty()) {
            return createErrorResponse("Could not determine the alert server url for the callback.");
        }

        UUID requestKey = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey, UUID.fromString(savedConfigModel.getId()));

        logger.info("OAuth authorization request created: {}", requestKey);
        String authUrl = createAuthURL(appId, requestKey);
        logger.debug("Authenticating Azure OAuth URL: {}", authUrl);
        return new ActionResponse<>(HttpStatus.OK, new OAuthEndpointResponse(isAuthenticated(savedConfigModel), authUrl, "Authenticating..."));
    }

    private ActionResponse<OAuthEndpointResponse> createErrorResponse(String errorMessage) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    private ActionResponse<OAuthEndpointResponse> createErrorResponse(HttpStatus httpStatus, String errorMessage) {
        OAuthEndpointResponse oAuthEndpointResponse = new OAuthEndpointResponse(false, "", errorMessage);
        return new ActionResponse<>(httpStatus, errorMessage, oAuthEndpointResponse);
    }

    private ActionResponse<AzureBoardsGlobalConfigModel> createOrUpdateModel(AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel) {
        if (!authorizationManager.hasExecutePermission(ConfigContextEnum.GLOBAL, ChannelKeys.AZURE_BOARDS)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ResponseFactory.UNAUTHORIZED_REQUEST_MESSAGE);
        }

        if (StringUtils.isNotBlank(azureBoardsGlobalConfigModel.getId())) {
            ActionResponse<AzureBoardsGlobalConfigModel> getOneResponse = azureBoardsGlobalCrudActions.getOne(UUID.fromString(azureBoardsGlobalConfigModel.getId()));
            if (getOneResponse.isError()) {
                return getOneResponse;
            }
            if (!getOneResponse.hasContent()) {
                return new ActionResponse<>(HttpStatus.NOT_FOUND, "Unable to find Azure Boards global config.");
            }
            return azureBoardsGlobalCrudActions.update(UUID.fromString(azureBoardsGlobalConfigModel.getId()), azureBoardsGlobalConfigModel);
        } else {
            return azureBoardsGlobalCrudActions.create(azureBoardsGlobalConfigModel);
        }
    }

    private boolean isAuthenticated(AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel) {
        AzureBoardsProperties properties = azureBoardsPropertiesFactory.fromGlobalConfigurationModel(
            alertOAuthCredentialDataStoreFactory,
            azureRedirectUrlCreator.createOAuthRedirectUri(),
            azureBoardsGlobalConfigModel
        );
        ProxyInfo proxy = proxyManager.createProxyInfoForHost(AzureHttpRequestCreatorFactory.DEFAULT_BASE_URL);
        return properties.hasOAuthCredentials(proxy);
    }

    private String createAuthURL(String clientId, UUID requestKey) {
        StringBuilder authUrlBuilder = new StringBuilder(300);
        authUrlBuilder.append(AzureHttpRequestCreatorFactory.DEFAULT_AUTHORIZATION_URL);
        authUrlBuilder.append(createQueryString(clientId, requestKey));
        return authUrlBuilder.toString();
    }

    private String createQueryString(String clientId, UUID requestKey) {
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
