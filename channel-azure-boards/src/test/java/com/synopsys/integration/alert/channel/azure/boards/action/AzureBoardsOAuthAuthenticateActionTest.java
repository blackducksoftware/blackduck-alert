package com.synopsys.integration.alert.channel.azure.boards.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.oauth.AlertOAuthCredentialDataStoreFactory;
import com.synopsys.integration.alert.api.oauth.database.accessor.AlertOAuthConfigurationAccessor;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUrlCreator;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.database.configuration.AzureBoardsConfigurationEntity;
import com.synopsys.integration.alert.channel.azure.boards.database.mock.MockAlertOAuthConfigurationRepository;
import com.synopsys.integration.alert.channel.azure.boards.database.mock.MockAzureBoardsConfigurationRepository;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.AlertWebServerUrlManager;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.alert.api.descriptor.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.database.MockRepositorySorter;

class AzureBoardsOAuthAuthenticateActionTest {
    private static final String SERVER_URL = "https://localhost:8443/alert";
    private static final String ORG_NAME = "alert_test_org_name";
    private static final String CLIENT_ID = "alert_test_client_id";
    private static final String CLIENT_SECRET = "alert_test_client_secret";
    private final Gson gson = new Gson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
    private AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;
    private final OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
    private AlertOAuthCredentialDataStoreFactory alertOAuthCredentialDataStoreFactory;
    private AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;
    private AzureBoardsGlobalConfigurationValidator azureBoardsGlobalConfigurationValidator;
    private AlertOAuthConfigurationAccessor alertOAuthConfigurationAccessor;
    private AlertWebServerUrlManager alertWebServerUrlManager;
    private AzureRedirectUrlCreator azureRedirectUrlCreator;
    private ProxyManager proxyManager = new ProxyManager(new MockSettingsUtility());
    private final AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
    private AzureBoardsGlobalCrudActions azureBoardsGlobalCrudActions;
    private AzureBoardsGlobalValidationAction validationAction;
    private AzureBoardsOAuthAuthenticateAction authenticateAction;

    @BeforeEach
    void init() {
        //Set up global config accessor
        MockRepositorySorter<AzureBoardsConfigurationEntity> sorter = new MockRepositorySorter<>();
        MockAzureBoardsConfigurationRepository mockAzureBoardsConfigurationRepository = new MockAzureBoardsConfigurationRepository(sorter);
        azureBoardsGlobalConfigAccessor = new AzureBoardsGlobalConfigAccessor(encryptionUtility, mockAzureBoardsConfigurationRepository);

        //Set up OAuth accessor
        MockAlertOAuthConfigurationRepository mockAlertOAuthConfigurationRepository = new MockAlertOAuthConfigurationRepository();
        alertOAuthConfigurationAccessor = new AlertOAuthConfigurationAccessor(mockAlertOAuthConfigurationRepository, encryptionUtility);
        alertOAuthCredentialDataStoreFactory = new AlertOAuthCredentialDataStoreFactory(alertOAuthConfigurationAccessor);

        //Set up azure global validation
        azureBoardsGlobalConfigurationValidator = new AzureBoardsGlobalConfigurationValidator(azureBoardsGlobalConfigAccessor);
        validationAction = new AzureBoardsGlobalValidationAction(azureBoardsGlobalConfigurationValidator, authorizationManager);

        //Set up AzureRedirectUrl and AlertWebServerManager
        alertWebServerUrlManager = new TestAlertWebServerUrlManager();
        azureRedirectUrlCreator = new AzureRedirectUrlCreator(alertWebServerUrlManager);

        // Set up CRUD actions
        azureBoardsGlobalCrudActions = new AzureBoardsGlobalCrudActions(
            authorizationManager,
            azureBoardsGlobalConfigAccessor,
            azureBoardsGlobalConfigurationValidator
        );

        // Set up Azure Properties Factory
        azureBoardsPropertiesFactory = new AzureBoardsPropertiesFactory(alertOAuthCredentialDataStoreFactory, azureRedirectUrlCreator, azureBoardsGlobalConfigAccessor, null);

        //Create the authenticateAction for testing
        authenticateAction = new AzureBoardsOAuthAuthenticateAction(
            authorizationManager,
            azureBoardsGlobalConfigAccessor,
            azureBoardsGlobalCrudActions,
            alertOAuthCredentialDataStoreFactory,
            azureBoardsPropertiesFactory,
            validationAction,
            azureRedirectUrlCreator,
            oAuthRequestValidator,
            alertWebServerUrlManager,
            proxyManager
        );
    }

    /**
     * Test authenticate with:
     * Run with full permissions, performing a create action on a model
     */
    @Test
    void authenticateTest() {
        AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel = new AzureBoardsGlobalConfigModel(
            null,
            "testConfigName",
            ORG_NAME,
            CLIENT_ID,
            CLIENT_SECRET
        );
        ActionResponse<OAuthEndpointResponse> oAuthEndpointResponseActionResponse = authenticateAction.authenticate(azureBoardsGlobalConfigModel);

        assertFalse(oAuthEndpointResponseActionResponse.isError());
        OAuthEndpointResponse oAuthEndpointResponse = oAuthEndpointResponseActionResponse.getContent().orElseThrow(() -> new AssertionError("oAuthEndpointResponse should exist."));
        String authorizationUrl = oAuthEndpointResponse.getAuthorizationUrl();
        assertFalse(authorizationUrl.contains(ORG_NAME), "Organization Name should not be included in the URL");
        assertTrue(authorizationUrl.contains(CLIENT_ID), "Missing Client ID");
        assertFalse(authorizationUrl.contains(CLIENT_SECRET), "Client secret should not be exposed in the authUrl");
        assertTrue(oAuthRequestValidator.hasRequests());
    }

    /**
     * Test authenticate with:
     * Run with full permissions, config model with missing fields causing a validation failures
     */
    @Test
    void testValidationErrors() {
        AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel = new AzureBoardsGlobalConfigModel(
            null,
            "testConfigName",
            null,
            null,
            null
        );
        ActionResponse<OAuthEndpointResponse> oAuthEndpointResponseActionResponse = authenticateAction.authenticate(azureBoardsGlobalConfigModel);
        assertTrue(oAuthEndpointResponseActionResponse.isError());
        assertEquals(HttpStatus.BAD_REQUEST, oAuthEndpointResponseActionResponse.getHttpStatus());
    }

    /**
     * Test authenticate with:
     * Run with full permissions, update rather than create is performed when the config model has an ID provided.
     */
    @Test
    void testAuthenticateWithUpdateAction() throws AlertConfigurationException {
        AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel = new AzureBoardsGlobalConfigModel(
            null,
            "testConfigName",
            "foo",
            "bar",
            "biz"
        );
        azureBoardsGlobalConfigAccessor.createConfiguration(azureBoardsGlobalConfigModel);
        AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModelUpdated = new AzureBoardsGlobalConfigModel(
            azureBoardsGlobalConfigModel.getId(),
            "testConfigName",
            ORG_NAME,
            CLIENT_ID,
            CLIENT_SECRET
        );
        ActionResponse<OAuthEndpointResponse> oAuthEndpointResponseActionResponse = authenticateAction.authenticate(azureBoardsGlobalConfigModelUpdated);
        assertFalse(oAuthEndpointResponseActionResponse.isError());
        OAuthEndpointResponse oAuthEndpointResponse = oAuthEndpointResponseActionResponse.getContent().orElseThrow(() -> new AssertionError("oAuthEndpointResponse should exist."));
        String authorizationUrl = oAuthEndpointResponse.getAuthorizationUrl();
        assertFalse(authorizationUrl.contains(ORG_NAME), "Organization Name should not be included in the URL");
        assertTrue(authorizationUrl.contains(CLIENT_ID), "Missing Client ID");
        assertFalse(authorizationUrl.contains(CLIENT_SECRET), "Client secret should not be exposed in the authUrl");
        assertTrue(oAuthRequestValidator.hasRequests());
    }

    /**
     * Test authenticate with:
     * full permissions, update performed on config model that has ID provided but no config is found in database
     */
    @Test
    void testUpdateFailure() {
        AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel = new AzureBoardsGlobalConfigModel(
            UUID.randomUUID().toString(),
            "testConfigName",
            ORG_NAME,
            CLIENT_ID,
            CLIENT_SECRET
        );
        ActionResponse<OAuthEndpointResponse> oAuthEndpointResponseActionResponse = authenticateAction.authenticate(azureBoardsGlobalConfigModel);
        assertTrue(oAuthEndpointResponseActionResponse.isError());
    }

    private AuthorizationManager createAuthorizationManager(int assignedPermissions) {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.AZURE_BOARDS;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, assignedPermissions);

        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    private static class TestAlertWebServerUrlManager implements AlertWebServerUrlManager {
        @Override
        public UriComponentsBuilder getServerComponentsBuilder() {
            return null;
        }

        @Override
        public Optional<String> getServerUrl(String... pathSegments) {
            return Optional.of(SERVER_URL);
        }
    }

    private static class MockSettingsUtility implements SettingsUtility {

        @Override
        public DescriptorKey getKey() {
            return null;
        }

        @Override
        public Optional<SettingsProxyModel> getConfiguration() {
            return Optional.empty();
        }
    }
}
