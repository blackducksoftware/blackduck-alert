package com.synopsys.integration.alert.channel.azure.boards.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.oauth.AlertOAuthCredentialDataStoreFactory;
import com.synopsys.integration.alert.api.oauth.database.accessor.AlertOAuthConfigurationAccessor;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUrlCreator;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.database.configuration.AzureBoardsConfigurationEntity;
import com.synopsys.integration.alert.channel.azure.boards.database.mock.MockAlertOAuthConfigurationRepository;
import com.synopsys.integration.alert.channel.azure.boards.database.mock.MockAzureBoardsConfigurationRepository;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.AlertWebServerUrlManager;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.database.MockRepositorySorter;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.service.project.TeamProjectReferenceResponseModel;

@ExtendWith(SpringExtension.class)
class AzureBoardsOAuthCallbackActionTest {
    private static final String SERVER_URL = "https://localhost:8443/alert";
    private static final String ORG_NAME = "alert_test_org_name";
    private static final String CLIENT_ID = "alert_test_client_id";
    private static final String CLIENT_SECRET = "alert_test_client_secret";
    private final Gson gson = new Gson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
    private final OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();

    private AlertOAuthCredentialDataStoreFactory alertOAuthCredentialDataStoreFactory;
    private AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;
    private AlertOAuthConfigurationAccessor alertOAuthConfigurationAccessor;
    private AzureRedirectUrlCreator azureRedirectUrlCreator;
    private AlertWebServerUrlManager alertWebServerUrlManager;
    private final AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
    private ProxyManager proxyManager = new ProxyManager(new MockSettingsUtility());
    private AzureBoardsOAuthCallbackAction callbackAction;

    @Mock
    private AzureBoardsPropertiesFactory azureBoardsPropertiesFactory;

    @BeforeEach
    void init() {
        //Set up global config accessor
        MockRepositorySorter<AzureBoardsConfigurationEntity> sorter = new MockRepositorySorter<>();
        MockAzureBoardsConfigurationRepository mockAzureBoardsConfigurationRepository = new MockAzureBoardsConfigurationRepository(sorter);
        azureBoardsGlobalConfigAccessor = new AzureBoardsGlobalConfigAccessor(encryptionUtility, mockAzureBoardsConfigurationRepository);

        //Set up OAuth accessor
        MockAlertOAuthConfigurationRepository mockAlertOAuthConfigurationRepository = new MockAlertOAuthConfigurationRepository();
        alertOAuthConfigurationAccessor = new AlertOAuthConfigurationAccessor(mockAlertOAuthConfigurationRepository);
        alertOAuthCredentialDataStoreFactory = new AlertOAuthCredentialDataStoreFactory(alertOAuthConfigurationAccessor);

        //Set up AzureRedirectUrl and AlertWebServerManager
        alertWebServerUrlManager = new TestAlertWebServerUrlManager();
        azureRedirectUrlCreator = new AzureRedirectUrlCreator(alertWebServerUrlManager);

        // Set up Azure Properties Factory
        //azureBoardsPropertiesFactory = new AzureBoardsPropertiesFactory(alertOAuthCredentialDataStoreFactory, azureRedirectUrlCreator, azureBoardsGlobalConfigAccessor, null);

        callbackAction = new AzureBoardsOAuthCallbackAction(
            alertOAuthCredentialDataStoreFactory,
            azureBoardsPropertiesFactory,
            azureBoardsGlobalConfigAccessor,
            gson,
            authorizationManager,
            proxyManager,
            oAuthRequestValidator,
            azureRedirectUrlCreator
        );
    }

    /**
     * Test with full permissions, ensure properties are created, and a successful redirect is created.
     * Note: This class cannot fully validate the oAuth connection to Azure. Azure services are mocked and the responses are tailored to this test to return a successful output.
     * @throws Exception
     */
    @Test
    void handleCallbackTest() throws Exception {
        UUID requestId = UUID.randomUUID();
        AzureBoardsGlobalConfigModel createModel = new AzureBoardsGlobalConfigModel(
            null,
            "testConfigName",
            ORG_NAME,
            CLIENT_ID,
            CLIENT_SECRET
        );
        AzureBoardsGlobalConfigModel savedModel = azureBoardsGlobalConfigAccessor.createConfiguration(createModel);
        String configId = savedModel.getId();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameter("state")).thenReturn(requestId.toString());
        Mockito.when(request.getRequestURI()).thenReturn("/alert/api/callbacks/oauth/azure");
        Mockito.when(request.getQueryString()).thenReturn("code/state");
        Mockito.when(request.getParameter("code")).thenReturn("authorizationCode");

        AzureBoardsProperties azureBoardsProperties = Mockito.mock(AzureBoardsProperties.class);
        Mockito.when(azureBoardsPropertiesFactory.fromGlobalConfigurationModel(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(azureBoardsProperties);

        AzureHttpService azureHttpService = Mockito.mock(AzureHttpService.class);
        Mockito.when(azureBoardsProperties.getOrganizationName()).thenReturn(ORG_NAME);
        Mockito.when(azureBoardsProperties.createAzureHttpService(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(azureHttpService);
        Mockito.when(azureBoardsProperties.createAzureHttpService(Mockito.any(), Mockito.any())).thenReturn(azureHttpService);
        AzureArrayResponseModel<TeamProjectReferenceResponseModel> azureArrayResponseModel = new AzureArrayResponseModel<>(0, List.of());
        Mockito.when(azureHttpService.get(Mockito.any(), Mockito.any())).thenReturn(azureArrayResponseModel);

        oAuthRequestValidator.addAuthorizationRequest(requestId, UUID.fromString(configId));

        ActionResponse<String> callbackURLResponse = callbackAction.handleCallback(request);

        assertFalse(callbackURLResponse.isError());
        assertEquals(SERVER_URL, callbackURLResponse.getContent().orElseThrow(() -> new AssertionError("Callback content missing.")));
        assertFalse(oAuthRequestValidator.hasRequests());
        Mockito.verify(azureBoardsPropertiesFactory).fromGlobalConfigurationModel(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void executePermissionFailure() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        AuthorizationManager authorizationManagerNoPermissions = createAuthorizationManager(AuthenticationTestUtils.NO_PERMISSIONS);
        callbackAction = new AzureBoardsOAuthCallbackAction(
            alertOAuthCredentialDataStoreFactory,
            azureBoardsPropertiesFactory,
            azureBoardsGlobalConfigAccessor,
            gson,
            authorizationManagerNoPermissions,
            proxyManager,
            oAuthRequestValidator,
            azureRedirectUrlCreator
        );
        ActionResponse<String> callbackURLResponse = callbackAction.handleCallback(request);

        assertTrue(callbackURLResponse.isError());
        assertEquals(HttpStatus.FORBIDDEN, callbackURLResponse.getHttpStatus());
    }

    /**
     * In the event that the state azure returns is invalid, it indicates that the response state was not returned as the generated UUID.
     * @throws Exception
     */
    @Test
    void invalidStateTest() throws Exception {
        UUID requestId = UUID.randomUUID();
        AzureBoardsGlobalConfigModel createModel = new AzureBoardsGlobalConfigModel(
            null,
            "testConfigName",
            ORG_NAME,
            CLIENT_ID,
            CLIENT_SECRET
        );
        AzureBoardsGlobalConfigModel savedModel = azureBoardsGlobalConfigAccessor.createConfiguration(createModel);
        String configId = savedModel.getId();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameter("state")).thenReturn("Invalid-Response-Without-UUID-RequestID");

        oAuthRequestValidator.addAuthorizationRequest(requestId, UUID.fromString(configId));

        ActionResponse<String> callbackURLResponse = callbackAction.handleCallback(request);

        assertTrue(callbackURLResponse.isError());
        assertEquals(HttpStatus.BAD_REQUEST, callbackURLResponse.getHttpStatus());
    }

    /**
     * If the callback is performed but reaches the timeout, a message is logged and the user is redirected to the configuration.
     * @throws Exception
     */
    @Test
    void validatorTimeoutNotFoundTest() throws Exception {
        UUID requestId = UUID.randomUUID();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameter("state")).thenReturn(requestId.toString());
        Mockito.when(request.getRequestURI()).thenReturn("/alert/api/callbacks/oauth/azure");
        Mockito.when(request.getQueryString()).thenReturn("code/state");
        Mockito.when(request.getParameter("code")).thenReturn("authorizationCode");

        ActionResponse<String> callbackURLResponse = callbackAction.handleCallback(request);

        assertFalse(callbackURLResponse.isError());
        assertEquals(SERVER_URL, callbackURLResponse.getContent().orElseThrow(() -> new AssertionError("Callback content missing.")));
        assertFalse(oAuthRequestValidator.hasRequests());
        Mockito.verify(azureBoardsPropertiesFactory, Mockito.times(0)).fromGlobalConfigurationModel(Mockito.any(), Mockito.any(), Mockito.any());
    }

    /**
     * If a configuration is missing after performing a callback (perhaps while waiting for the callback, the configuration was deleted) the request ID is still present but the
     * configuration no longer exists. In this case we should return to the default redirect.
     * @throws Exception
     */
    @Test
    void globalConfigurationMissingAfterCallback() throws Exception {
        UUID requestId = UUID.randomUUID();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameter("state")).thenReturn(requestId.toString());
        Mockito.when(request.getRequestURI()).thenReturn("/alert/api/callbacks/oauth/azure");
        Mockito.when(request.getQueryString()).thenReturn("code/state");
        Mockito.when(request.getParameter("code")).thenReturn("authorizationCode");

        oAuthRequestValidator.addAuthorizationRequest(requestId, UUID.randomUUID());

        ActionResponse<String> callbackURLResponse = callbackAction.handleCallback(request);

        assertFalse(callbackURLResponse.isError());
        assertEquals(SERVER_URL, callbackURLResponse.getContent().orElseThrow(() -> new AssertionError("Callback content missing.")));
        assertFalse(oAuthRequestValidator.hasRequests());
        Mockito.verify(azureBoardsPropertiesFactory, Mockito.times(0)).fromGlobalConfigurationModel(Mockito.any(), Mockito.any(), Mockito.any());
    }

    /**
     * If a response is returned, but the authorizationn code is invalid or missing, we should return a failure for the oauth callback. To simulate this, the mocked server request
     * returns an empty code. Expect to return a redirect to the default location
     * @throws Exception
     */
    @Test
    void noAuthorizationCodeTest() throws Exception {
        UUID requestId = UUID.randomUUID();
        AzureBoardsGlobalConfigModel createModel = new AzureBoardsGlobalConfigModel(
            null,
            "testConfigName",
            ORG_NAME,
            CLIENT_ID,
            CLIENT_SECRET
        );
        AzureBoardsGlobalConfigModel savedModel = azureBoardsGlobalConfigAccessor.createConfiguration(createModel);
        String configId = savedModel.getId();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameter("state")).thenReturn(requestId.toString());
        Mockito.when(request.getRequestURI()).thenReturn("/alert/api/callbacks/oauth/azure");
        Mockito.when(request.getQueryString()).thenReturn("code/state");
        Mockito.when(request.getParameter("code")).thenReturn("");

        oAuthRequestValidator.addAuthorizationRequest(requestId, UUID.fromString(configId));

        ActionResponse<String> callbackURLResponse = callbackAction.handleCallback(request);

        assertFalse(callbackURLResponse.isError());
        assertEquals(SERVER_URL, callbackURLResponse.getContent().orElseThrow(() -> new AssertionError("Callback content missing.")));
        assertFalse(oAuthRequestValidator.hasRequests());
        Mockito.verify(azureBoardsPropertiesFactory, Mockito.times(0)).fromGlobalConfigurationModel(Mockito.any(), Mockito.any(), Mockito.any());
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
