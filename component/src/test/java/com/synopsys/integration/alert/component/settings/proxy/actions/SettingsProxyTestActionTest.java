package com.synopsys.integration.alert.component.settings.proxy.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.ConfigurationTestResult;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyTestService;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.proxy.action.SettingsProxyTestAction;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;
import com.synopsys.integration.alert.database.settings.proxy.NonProxyHostsConfigurationRepository;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationEntity;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationRepository;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

class SettingsProxyTestActionTest {
    private static final String TEST_URL = "https://testUrl";
    private static final String HOST = "hostname";
    private static final Integer PORT = 12345;
    private static final String USERNAME = "userName";
    private static final String PASSWORD = "password";

    private final Gson gson = new Gson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

    private final AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
    private final DescriptorKey descriptorKey = new SettingsDescriptorKey();
    private final PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    private final Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
    private final AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));

    private final SettingsProxyValidator settingsProxyValidator = new SettingsProxyValidator();
    private final SettingsDescriptorKey settingsDescriptorKey = new SettingsDescriptorKey();

    private SettingsProxyConfigAccessor settingsProxyConfigAccessor;
    private SettingsProxyModel defaultSettingsProxyModel = createDefaultSettingsProxyModel();

    @BeforeEach
    void init() {
        UUID uuid = UUID.randomUUID();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);
        Mockito.when(settingsProxyConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(createSettingsProxyConfigurationEntity(uuid)));
        settingsProxyConfigAccessor = new SettingsProxyConfigAccessor(encryptionUtility, settingsProxyConfigurationRepository, nonProxyHostsConfigurationRepository);
    }

    @Test
    void testWithPermissionCheckTest() {
        ConfigurationTestResult configurationTestResult = ConfigurationTestResult.success();
        ProxyTestService proxyTestService = Mockito.mock(ProxyTestService.class);
        Mockito.when(proxyTestService.pingHost(Mockito.eq(TEST_URL), Mockito.any())).thenReturn(configurationTestResult);

        SettingsProxyTestAction settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);
        ActionResponse<ValidationResponseModel> actionResponse = settingsProxyTestAction.testWithPermissionCheck(TEST_URL, defaultSettingsProxyModel);

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        Optional<ValidationResponseModel> validationResponseModel = actionResponse.getContent();
        assertTrue(validationResponseModel.isPresent());
        assertFalse(validationResponseModel.get().hasErrors());
    }

    @Test
    void testWithPermissionCheckFailureTest() {
        ConfigurationTestResult configurationTestResult = ConfigurationTestResult.failure("Failure");
        ProxyTestService proxyTestService = Mockito.mock(ProxyTestService.class);
        Mockito.when(proxyTestService.pingHost(Mockito.eq(TEST_URL), Mockito.any())).thenReturn(configurationTestResult);

        SettingsProxyTestAction settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);
        ActionResponse<ValidationResponseModel> actionResponse = settingsProxyTestAction.testWithPermissionCheck(TEST_URL, defaultSettingsProxyModel);

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        Optional<ValidationResponseModel> validationResponseModel = actionResponse.getContent();
        assertTrue(validationResponseModel.isPresent());
        assertTrue(validationResponseModel.get().hasErrors());
    }

    @Test
    void testConfigModelContentTest() {
        ConfigurationTestResult configurationTestResult = ConfigurationTestResult.success();
        ProxyTestService proxyTestService = Mockito.mock(ProxyTestService.class);
        Mockito.when(proxyTestService.pingHost(Mockito.eq(TEST_URL), Mockito.any())).thenReturn(configurationTestResult);

        SettingsProxyTestAction settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);
        ConfigurationTestResult testResult = settingsProxyTestAction.testConfigModelContent(TEST_URL, defaultSettingsProxyModel);

        assertEquals(configurationTestResult, testResult);
    }

    @Test
    void blankTestUrlTest() {
        ProxyTestService proxyTestService = Mockito.mock(ProxyTestService.class);

        SettingsProxyTestAction settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);
        ConfigurationTestResult testResult = settingsProxyTestAction.testConfigModelContent("", defaultSettingsProxyModel);

        assertFalse(testResult.isSuccess());
    }

    @Test
    void passwordAlreadySavedTest() {
        ConfigurationTestResult configurationTestResult = ConfigurationTestResult.success();
        ProxyTestService proxyTestService = Mockito.mock(ProxyTestService.class);
        Mockito.when(proxyTestService.pingHost(Mockito.eq(TEST_URL), Mockito.any())).thenReturn(configurationTestResult);

        SettingsProxyModel settingsProxyModel = createDefaultSettingsProxyModel();
        settingsProxyModel.setProxyPassword(null);
        settingsProxyModel.setIsProxyPasswordSet(true);

        SettingsProxyTestAction settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);
        ConfigurationTestResult testResult = settingsProxyTestAction.testConfigModelContent(TEST_URL, settingsProxyModel);

        assertEquals(configurationTestResult, testResult);
    }

    private SettingsProxyConfigurationEntity createSettingsProxyConfigurationEntity(UUID id) {
        return new SettingsProxyConfigurationEntity(
            id,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(),
            HOST,
            PORT,
            USERNAME,
            encryptionUtility.encrypt(PASSWORD),
            List.of()
        );
    }

    private SettingsProxyModel createDefaultSettingsProxyModel() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        settingsProxyModel.setProxyHost(HOST);
        settingsProxyModel.setProxyPort(PORT);
        settingsProxyModel.setProxyUsername(USERNAME);
        settingsProxyModel.setIsProxyPasswordSet(false);
        settingsProxyModel.setProxyPassword(PASSWORD);
        return settingsProxyModel;
    }
}
