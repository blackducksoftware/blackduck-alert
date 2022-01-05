package com.synopsys.integration.alert.component.settings.proxy.actions;

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
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.proxy.action.SettingsProxyCrudActions;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;
import com.synopsys.integration.alert.database.settings.proxy.NonProxyHostsConfigurationRepository;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationEntity;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationRepository;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

public class SettingsProxyCrudActionsTest {
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

    @Test
    public void getOneTest() {
        UUID uuid = UUID.randomUUID();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);

        Mockito.when(settingsProxyConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(createSettingsProxyConfigurationEntity(uuid)));

        SettingsProxyConfigAccessor settingsProxyConfigAccessor = new SettingsProxyConfigAccessor(encryptionUtility, settingsProxyConfigurationRepository, nonProxyHostsConfigurationRepository);

        SettingsProxyCrudActions configActions = new SettingsProxyCrudActions(authorizationManager, settingsProxyConfigAccessor, settingsProxyValidator, settingsDescriptorKey);
        ActionResponse<SettingsProxyModel> actionResponse = configActions.getOne();

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertModelObfuscated(actionResponse);
    }

    @Test
    public void createTest() {
        UUID uuid = UUID.randomUUID();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);

        SettingsProxyConfigurationEntity entity = createSettingsProxyConfigurationEntity(uuid);
        Mockito.when(settingsProxyConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.empty());

        Mockito.when(settingsProxyConfigurationRepository.save(Mockito.any())).thenReturn(entity);
        Mockito.when(settingsProxyConfigurationRepository.getOne(uuid)).thenReturn(entity);

        SettingsProxyConfigAccessor settingsProxyConfigAccessor = new SettingsProxyConfigAccessor(encryptionUtility, settingsProxyConfigurationRepository, nonProxyHostsConfigurationRepository);

        SettingsProxyCrudActions configActions = new SettingsProxyCrudActions(authorizationManager, settingsProxyConfigAccessor, settingsProxyValidator, settingsDescriptorKey);
        ActionResponse<SettingsProxyModel> actionResponse = configActions.create(createSettingsProxyModel());

        Mockito.verify(nonProxyHostsConfigurationRepository).saveAll(Mockito.any());
        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertModelObfuscated(actionResponse);
    }

    @Test
    public void createTestConfigAlreadyExists() {
        UUID uuid = UUID.randomUUID();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);

        Mockito.when(settingsProxyConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(createSettingsProxyConfigurationEntity(uuid)));

        SettingsProxyConfigAccessor settingsProxyConfigAccessor = new SettingsProxyConfigAccessor(encryptionUtility, settingsProxyConfigurationRepository, nonProxyHostsConfigurationRepository);

        SettingsProxyCrudActions configActions = new SettingsProxyCrudActions(authorizationManager, settingsProxyConfigAccessor, settingsProxyValidator, settingsDescriptorKey);
        ActionResponse<SettingsProxyModel> actionResponse = configActions.create(createSettingsProxyModel());

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actionResponse.getHttpStatus());
    }

    @Test
    public void updateTest() {
        UUID uuid = UUID.randomUUID();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);

        SettingsProxyConfigurationEntity entity = createSettingsProxyConfigurationEntity(uuid);
        Mockito.when(settingsProxyConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(entity));
        Mockito.when(settingsProxyConfigurationRepository.save(Mockito.any())).thenReturn(entity);
        Mockito.when(settingsProxyConfigurationRepository.getOne(uuid)).thenReturn(entity);

        SettingsProxyConfigAccessor settingsProxyConfigAccessor = new SettingsProxyConfigAccessor(encryptionUtility, settingsProxyConfigurationRepository, nonProxyHostsConfigurationRepository);

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
    public void deleteTest() {
        UUID uuid = UUID.randomUUID();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);

        Mockito.when(settingsProxyConfigurationRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)).thenReturn(Optional.of(createSettingsProxyConfigurationEntity(uuid)));

        SettingsProxyConfigAccessor settingsProxyConfigAccessor = new SettingsProxyConfigAccessor(encryptionUtility, settingsProxyConfigurationRepository, nonProxyHostsConfigurationRepository);

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
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        settingsProxyModel.setProxyHost(HOST);
        settingsProxyModel.setProxyPort(PORT);
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
        assertTrue(settingsProxyModel.getProxyHost().isPresent());
        assertTrue(settingsProxyModel.getProxyPort().isPresent());
        assertTrue(settingsProxyModel.getProxyUsername().isPresent());
        assertTrue(settingsProxyModel.getProxyPassword().isEmpty());
        assertTrue(settingsProxyModel.getIsProxyPasswordSet());

        assertEquals(HOST, settingsProxyModel.getProxyHost().get());
        assertEquals(PORT, settingsProxyModel.getProxyPort().get());
        assertEquals(USERNAME, settingsProxyModel.getProxyUsername().get());
    }
}
