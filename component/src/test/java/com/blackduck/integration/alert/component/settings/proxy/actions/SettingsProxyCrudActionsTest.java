package com.blackduck.integration.alert.component.settings.proxy.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.google.gson.Gson;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.blackduck.integration.alert.component.settings.proxy.action.SettingsProxyCrudActions;
import com.blackduck.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.blackduck.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;
import com.synopsys.integration.alert.database.settings.proxy.NonProxyHostsConfigurationRepository;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationEntity;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationRepository;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

class SettingsProxyCrudActionsTest {
    private static final String HOST = "hostname";
    private static final Integer PORT = 12345;
    private static final String USERNAME = "userName";
    private static final String PASSWORD = "password";

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

    private final AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
    private final DescriptorKey descriptorKey = new SettingsDescriptorKey();
    private final PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    private final Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
    private final AuthorizationManager authorizationManager = authenticationTestUtils
        .createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));

    private final SettingsProxyValidator settingsProxyValidator = new SettingsProxyValidator();
    private final SettingsDescriptorKey settingsDescriptorKey = new SettingsDescriptorKey();

    @Test
    void getOneTest() {
        UUID uuid = UUID.randomUUID();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);

        Mockito.when(settingsProxyConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME))
            .thenReturn(Optional.of(createSettingsProxyConfigurationEntity(uuid)));

        SettingsProxyConfigAccessor settingsProxyConfigAccessor = new SettingsProxyConfigAccessor(
            encryptionUtility,
            settingsProxyConfigurationRepository,
            nonProxyHostsConfigurationRepository
        );

        SettingsProxyCrudActions configActions = new SettingsProxyCrudActions(authorizationManager, settingsProxyConfigAccessor, settingsProxyValidator, settingsDescriptorKey);
        ActionResponse<SettingsProxyModel> actionResponse = configActions.getOne();

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertModelObfuscated(actionResponse);
    }

    @Test
    void createTest() {
        UUID uuid = UUID.randomUUID();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);

        SettingsProxyConfigurationEntity entity = createSettingsProxyConfigurationEntity(uuid);
        Mockito.when(settingsProxyConfigurationRepository.existsByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(false);

        Mockito.when(settingsProxyConfigurationRepository.save(Mockito.any())).thenReturn(entity);
        Mockito.when(settingsProxyConfigurationRepository.getOne(uuid)).thenReturn(entity);

        SettingsProxyConfigAccessor settingsProxyConfigAccessor = new SettingsProxyConfigAccessor(
            encryptionUtility,
            settingsProxyConfigurationRepository,
            nonProxyHostsConfigurationRepository
        );

        SettingsProxyCrudActions configActions = new SettingsProxyCrudActions(authorizationManager, settingsProxyConfigAccessor, settingsProxyValidator, settingsDescriptorKey);
        ActionResponse<SettingsProxyModel> actionResponse = configActions.create(createSettingsProxyModel());

        Mockito.verify(nonProxyHostsConfigurationRepository).saveAll(Mockito.any());
        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertModelObfuscated(actionResponse);
    }

    @Test
    void createTestConfigAlreadyExists() {
        UUID uuid = UUID.randomUUID();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);

        Mockito.when(settingsProxyConfigurationRepository.existsByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(true);

        SettingsProxyConfigAccessor settingsProxyConfigAccessor = new SettingsProxyConfigAccessor(
            encryptionUtility,
            settingsProxyConfigurationRepository,
            nonProxyHostsConfigurationRepository
        );

        SettingsProxyCrudActions configActions = new SettingsProxyCrudActions(authorizationManager, settingsProxyConfigAccessor, settingsProxyValidator, settingsDescriptorKey);
        ActionResponse<SettingsProxyModel> actionResponse = configActions.create(createSettingsProxyModel());

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

    @Test
    void updateTest() {
        UUID uuid = UUID.randomUUID();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);

        SettingsProxyConfigurationEntity entity = createSettingsProxyConfigurationEntity(uuid);
        Mockito.when(settingsProxyConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(entity));
        Mockito.when(settingsProxyConfigurationRepository.existsByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(true);
        Mockito.when(settingsProxyConfigurationRepository.save(Mockito.any())).thenReturn(entity);
        Mockito.when(settingsProxyConfigurationRepository.getOne(uuid)).thenReturn(entity);

        SettingsProxyConfigAccessor settingsProxyConfigAccessor = new SettingsProxyConfigAccessor(
            encryptionUtility,
            settingsProxyConfigurationRepository,
            nonProxyHostsConfigurationRepository
        );

        SettingsProxyCrudActions configActions = new SettingsProxyCrudActions(authorizationManager, settingsProxyConfigAccessor, settingsProxyValidator, settingsDescriptorKey);
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel();
        ActionResponse<SettingsProxyModel> actionResponse = configActions.update(settingsProxyModel);

        Mockito.verify(nonProxyHostsConfigurationRepository).saveAll(Mockito.any());
        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertModelObfuscated(actionResponse);
    }

    @Test
    void deleteTest() {
        UUID uuid = UUID.randomUUID();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);

        Mockito.when(settingsProxyConfigurationRepository.existsByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(true);

        SettingsProxyConfigAccessor settingsProxyConfigAccessor = new SettingsProxyConfigAccessor(
            encryptionUtility,
            settingsProxyConfigurationRepository,
            nonProxyHostsConfigurationRepository
        );

        SettingsProxyCrudActions configActions = new SettingsProxyCrudActions(authorizationManager, settingsProxyConfigAccessor, settingsProxyValidator, settingsDescriptorKey);
        ActionResponse<SettingsProxyModel> actionResponse = configActions.delete();

        Mockito.verify(settingsProxyConfigurationRepository).deleteByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(actionResponse.isSuccessful());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, actionResponse.getHttpStatus());
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

    private SettingsProxyModel createSettingsProxyModel() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, HOST, PORT);
        settingsProxyModel.setProxyUsername(USERNAME);
        settingsProxyModel.setProxyPassword(PASSWORD);
        return settingsProxyModel;
    }

    private void assertModelObfuscated(ActionResponse<SettingsProxyModel> actionResponse) {
        Optional<SettingsProxyModel> optionalSettingsProxyModel = actionResponse.getContent();
        assertTrue(optionalSettingsProxyModel.isPresent());
        assertModelObfuscated(optionalSettingsProxyModel.get());
    }

    private void assertModelObfuscated(SettingsProxyModel settingsProxyModel) {
        assertTrue(settingsProxyModel.getProxyUsername().isPresent());
        assertTrue(settingsProxyModel.getProxyPassword().isEmpty());
        assertTrue(settingsProxyModel.getIsProxyPasswordSet());

        assertEquals(HOST, settingsProxyModel.getProxyHost());
        assertEquals(PORT, settingsProxyModel.getProxyPort());
        assertEquals(USERNAME, settingsProxyModel.getProxyUsername().get());
    }
}
