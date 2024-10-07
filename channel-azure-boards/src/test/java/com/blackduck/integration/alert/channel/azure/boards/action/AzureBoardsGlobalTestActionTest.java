package com.blackduck.integration.alert.channel.azure.boards.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.oauth.AlertOAuthCredentialDataStoreFactory;
import com.blackduck.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.blackduck.integration.alert.channel.azure.boards.AzureRedirectUrlCreator;
import com.blackduck.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.azure.boards.database.configuration.AzureBoardsConfigurationEntity;
import com.blackduck.integration.alert.channel.azure.boards.database.mock.MockAzureBoardsConfigurationRepository;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.message.model.ConfigurationTestResult;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.alert.test.common.database.MockRepositorySorter;
import com.blackduck.integration.alert.azure.boards.common.service.project.AzureProjectService;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;

@ExtendWith(SpringExtension.class)
class AzureBoardsGlobalTestActionTest {
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
    AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;
    AzureBoardsGlobalConfigurationValidator azureBoardsGlobalConfigurationValidator;

    @Mock
    AlertOAuthCredentialDataStoreFactory mockAlertOAuthCredentialDataStoreFactory;
    @Mock
    AzureBoardsPropertiesFactory mockAzureBoardsPropertiesFactory;
    @Mock
    AzureProjectService mockedAzureProjectService;
    @Mock
    AzureRedirectUrlCreator mockAzureRedirectUrlCreator;
    @Mock
    ProxyManager mockProxyManager;

    @BeforeEach
    void initEach() {
        MockRepositorySorter<AzureBoardsConfigurationEntity> sorter = new MockRepositorySorter<>();
        MockAzureBoardsConfigurationRepository mockAzureBoardsConfigurationRepository = new MockAzureBoardsConfigurationRepository(sorter);
        azureBoardsGlobalConfigAccessor = new AzureBoardsGlobalConfigAccessor(encryptionUtility, mockAzureBoardsConfigurationRepository);
        azureBoardsGlobalConfigurationValidator = new AzureBoardsGlobalConfigurationValidator(azureBoardsGlobalConfigAccessor);

        Mockito.when(mockAzureRedirectUrlCreator.createOAuthRedirectUri()).thenReturn("https://www.redirect.com");
    }

    @Test
    void testWithPermissionCheckReturnsHttpForbidden() {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.NO_PERMISSIONS);
        AzureBoardsGlobalConfigModel requestModel = Mockito.mock(AzureBoardsGlobalConfigModel.class);

        AzureBoardsGlobalTestAction azureBoardsGlobalTestAction = new AzureBoardsGlobalTestAction(
            authorizationManager,
            azureBoardsGlobalConfigurationValidator,
            azureBoardsGlobalConfigAccessor,
            mockAlertOAuthCredentialDataStoreFactory,
            mockAzureBoardsPropertiesFactory,
            mockAzureRedirectUrlCreator,
            gson,
            mockProxyManager
        );

        ActionResponse<ValidationResponseModel> actionResponse = azureBoardsGlobalTestAction.testWithPermissionCheck(requestModel);
        assertTrue(actionResponse.isError());
        assertEquals(HttpStatus.FORBIDDEN, actionResponse.getHttpStatus());
    }

    @Test
    void testWithPermissionCheckReturnsHttpOk() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        AzureBoardsGlobalConfigModel requestModel = Mockito.mock(AzureBoardsGlobalConfigModel.class);

        AzureBoardsGlobalTestAction spiedAzureBoardsGlobalTestAction = Mockito.spy(
            new AzureBoardsGlobalTestAction(
                authorizationManager,
                azureBoardsGlobalConfigurationValidator,
                azureBoardsGlobalConfigAccessor,
                mockAlertOAuthCredentialDataStoreFactory,
                mockAzureBoardsPropertiesFactory,
                mockAzureRedirectUrlCreator,
                gson,
                mockProxyManager
            )
        );
        Mockito.doReturn(mockedAzureProjectService).when(spiedAzureBoardsGlobalTestAction).createAzureProjectService(any());

        ActionResponse<ValidationResponseModel> actionResponse = spiedAzureBoardsGlobalTestAction.testWithPermissionCheck(requestModel);
        assertTrue(actionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
    }

    @Test
    void testConfigModelContentSetsObfuscatedFields() throws IntegrationException {
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        String name = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
        String organizationName = "Organization name";
        String appId = "obfuscated-app-id";
        String clientSecret = "obfuscatedClientSecret";
        AzureBoardsGlobalConfigModel createdModel = azureBoardsGlobalConfigAccessor.createConfiguration(
            new AzureBoardsGlobalConfigModel(
                "",
                name,
                "",
                "",
                organizationName,
                appId,
                Boolean.TRUE,
                clientSecret,
                Boolean.TRUE
            )
        );
        String modelId = createdModel.getId();

        AzureBoardsGlobalTestAction spiedAzureBoardsGlobalTestAction = Mockito.spy(
            new AzureBoardsGlobalTestAction(
                authorizationManager,
                azureBoardsGlobalConfigurationValidator,
                azureBoardsGlobalConfigAccessor,
                mockAlertOAuthCredentialDataStoreFactory,
                mockAzureBoardsPropertiesFactory,
                mockAzureRedirectUrlCreator,
                gson,
                mockProxyManager
            )
        );
        Mockito.doReturn(mockedAzureProjectService).when(spiedAzureBoardsGlobalTestAction).createAzureProjectService(any());

        AzureBoardsGlobalConfigModel requestModel = new AzureBoardsGlobalConfigModel(
            modelId,
            name,
            "",
            "",
            organizationName,
            "", // appId is hidden
            Boolean.TRUE,
            "", // clientSecret is hidden
            Boolean.TRUE
        );
        assertEquals("", requestModel.getAppId().orElse("Not empty app id"));
        assertEquals("", requestModel.getAppId().orElse("Not empty client secret"));

        ConfigurationTestResult configurationTestResult = spiedAzureBoardsGlobalTestAction.testConfigModelContent(requestModel);

        assertTrue(configurationTestResult.isSuccess());

        assertEquals(appId, requestModel.getAppId().orElse("Not the saved app id"));
        assertEquals(clientSecret, requestModel.getClientSecret().orElse("Not the saved client secret"));
    }

    private AuthorizationManager createAuthorizationManager(int assignedPermissions) {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.AZURE_BOARDS;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, assignedPermissions);

        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }
}
