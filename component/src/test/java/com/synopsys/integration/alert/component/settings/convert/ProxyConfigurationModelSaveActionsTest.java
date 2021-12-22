package com.synopsys.integration.alert.component.settings.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.proxy.action.SettingsProxyCrudActions;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;
import com.synopsys.integration.alert.database.settings.proxy.NonProxyHostConfigurationEntity;
import com.synopsys.integration.alert.database.settings.proxy.NonProxyHostsConfigurationRepository;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationEntity;
import com.synopsys.integration.alert.database.settings.proxy.SettingsProxyConfigurationRepository;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;

class ProxyConfigurationModelSaveActionsTest {
    public static final String TEST_PROXY_HOST = "host";
    public static final String TEST_PROXY_PORT = "9999";
    public static final String TEST_PROXY_USERNAME = "username";
    public static final String TEST_PROXY_PASSWORD = "password";
    public static final String TEST_PROXY_NON_PROXY_HOST = "nonProxyHostUrl";

    private final SettingsDescriptorKey settingsDescriptorKey = new SettingsDescriptorKey();

    private final Gson gson = new Gson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
    private final AuthorizationManager authorizationManager = createAuthorizationManager();
    private final ProxyConfigurationModelConverter converter = new ProxyConfigurationModelConverter();
    private final SettingsProxyValidator validator = new SettingsProxyValidator();

    @Test
    void getDescriptorKeyTest() {
        ProxyConfigurationModelSaveActions saveActions = new ProxyConfigurationModelSaveActions(null, null);
        assertEquals(settingsDescriptorKey, saveActions.getDescriptorKey());
    }

    @Test
    void createTest() {
        AtomicReference<SettingsProxyConfigurationEntity> savedEntity = new AtomicReference<>();
        AtomicReference<NonProxyHostConfigurationEntity> savedNonProxyHostEntity = new AtomicReference<>();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);
        Mockito.when(settingsProxyConfigurationRepository.save(Mockito.any(SettingsProxyConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });
        Mockito.when(nonProxyHostsConfigurationRepository.saveAll(Mockito.any(List.class))).thenAnswer(invocation -> {
            Iterable<NonProxyHostConfigurationEntity> iterable = invocation.getArgument(0);
            for (NonProxyHostConfigurationEntity entity : iterable) {
                savedNonProxyHostEntity.set(entity);
            }
            return List.of(savedNonProxyHostEntity.get());
        });

        SettingsProxyConfigAccessor configurationAccessor = new SettingsProxyConfigAccessor(encryptionUtility, settingsProxyConfigurationRepository, nonProxyHostsConfigurationRepository);
        SettingsProxyCrudActions settingsProxyCrudActions = new SettingsProxyCrudActions(authorizationManager, configurationAccessor, validator, settingsDescriptorKey);
        ProxyConfigurationModelSaveActions saveActions = new ProxyConfigurationModelSaveActions(converter, settingsProxyCrudActions);
        saveActions.createConcreteModel(createDefaultConfigurationModel());

        SettingsProxyConfigurationEntity actualEntity = savedEntity.get();
        assertEquals(TEST_PROXY_HOST, actualEntity.getHost());
        assertEquals(Integer.valueOf(TEST_PROXY_PORT), actualEntity.getPort());
        assertEquals(TEST_PROXY_USERNAME, actualEntity.getUsername());
        assertEquals(TEST_PROXY_PASSWORD, encryptionUtility.decrypt(actualEntity.getPassword()));

        NonProxyHostConfigurationEntity actualNonProxyHosts = savedNonProxyHostEntity.get();
        assertNotNull(actualNonProxyHosts);
        assertEquals(TEST_PROXY_NON_PROXY_HOST, actualNonProxyHosts.getHostnamePattern());
    }

    @Test
    void createInvalidConversionTest() {
        AtomicReference<SettingsProxyConfigurationEntity> savedEntity = new AtomicReference<>();
        AtomicReference<NonProxyHostConfigurationEntity> savedNonProxyHostEntity = new AtomicReference<>();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);
        Mockito.when(settingsProxyConfigurationRepository.save(Mockito.any(SettingsProxyConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });
        Mockito.when(nonProxyHostsConfigurationRepository.saveAll(Mockito.any(List.class))).thenAnswer(invocation -> {
            Iterable<NonProxyHostConfigurationEntity> iterable = invocation.getArgument(0);
            for (NonProxyHostConfigurationEntity entity : iterable) {
                savedNonProxyHostEntity.set(entity);
            }
            return List.of(savedNonProxyHostEntity.get());
        });

        SettingsProxyConfigAccessor configurationAccessor = new SettingsProxyConfigAccessor(encryptionUtility, settingsProxyConfigurationRepository, nonProxyHostsConfigurationRepository);
        SettingsProxyCrudActions settingsProxyCrudActions = new SettingsProxyCrudActions(authorizationManager, configurationAccessor, validator, settingsDescriptorKey);
        ProxyConfigurationModelSaveActions saveActions = new ProxyConfigurationModelSaveActions(converter, settingsProxyCrudActions);
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        updateField(configurationModel, ProxyConfigurationModelConverter.FIELD_KEY_PORT, "badport");
        saveActions.createConcreteModel(configurationModel);

        SettingsProxyConfigurationEntity actualEntity = savedEntity.get();
        NonProxyHostConfigurationEntity actualNonProxyHosts = savedNonProxyHostEntity.get();
        assertNull(actualEntity);
        assertNull(actualNonProxyHosts);
    }

    @Test
    void updateTest() {
        AtomicReference<SettingsProxyConfigurationEntity> savedEntity = new AtomicReference<>();
        AtomicReference<NonProxyHostConfigurationEntity> savedNonProxyHostEntity = new AtomicReference<>();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);
        Mockito.when(settingsProxyConfigurationRepository.save(Mockito.any(SettingsProxyConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        Mockito.when(settingsProxyConfigurationRepository.findByName(Mockito.anyString())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));

        Mockito.when(nonProxyHostsConfigurationRepository.saveAll(Mockito.any(List.class))).thenAnswer(invocation -> {
            Iterable<NonProxyHostConfigurationEntity> iterable = invocation.getArgument(0);
            for (NonProxyHostConfigurationEntity entity : iterable) {
                savedNonProxyHostEntity.set(entity);
            }
            return List.of(savedNonProxyHostEntity.get());
        });

        SettingsProxyConfigAccessor configurationAccessor = new SettingsProxyConfigAccessor(encryptionUtility, settingsProxyConfigurationRepository, nonProxyHostsConfigurationRepository);
        SettingsProxyCrudActions settingsProxyCrudActions = new SettingsProxyCrudActions(authorizationManager, configurationAccessor, validator, settingsDescriptorKey);
        ProxyConfigurationModelSaveActions saveActions = new ProxyConfigurationModelSaveActions(converter, settingsProxyCrudActions);
        String newHost = "updatedHost";
        String newPort = "1234";
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        saveActions.createConcreteModel(configurationModel);

        updateField(configurationModel, ProxyConfigurationModelConverter.FIELD_KEY_HOST, newHost);
        updateField(configurationModel, ProxyConfigurationModelConverter.FIELD_KEY_PORT, newPort);

        saveActions.updateConcreteModel(configurationModel);

        SettingsProxyConfigurationEntity actualEntity = savedEntity.get();
        assertEquals(newHost, actualEntity.getHost());
        assertEquals(Integer.valueOf(newPort), actualEntity.getPort());
        assertEquals(TEST_PROXY_USERNAME, actualEntity.getUsername());
        assertEquals(TEST_PROXY_PASSWORD, encryptionUtility.decrypt(actualEntity.getPassword()));

        NonProxyHostConfigurationEntity actualNonProxyHosts = savedNonProxyHostEntity.get();
        assertNotNull(actualNonProxyHosts);
        assertEquals(TEST_PROXY_NON_PROXY_HOST, actualNonProxyHosts.getHostnamePattern());
    }

    @Test
    void updateInvalidConversionTest() {
        AtomicReference<SettingsProxyConfigurationEntity> savedEntity = new AtomicReference<>();
        AtomicReference<NonProxyHostConfigurationEntity> savedNonProxyHostEntity = new AtomicReference<>();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);
        Mockito.when(settingsProxyConfigurationRepository.save(Mockito.any(SettingsProxyConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        Mockito.when(settingsProxyConfigurationRepository.findByName(Mockito.anyString())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));

        Mockito.when(nonProxyHostsConfigurationRepository.saveAll(Mockito.any(List.class))).thenAnswer(invocation -> {
            Iterable<NonProxyHostConfigurationEntity> iterable = invocation.getArgument(0);
            for (NonProxyHostConfigurationEntity entity : iterable) {
                savedNonProxyHostEntity.set(entity);
            }
            return List.of(savedNonProxyHostEntity.get());
        });

        SettingsProxyConfigAccessor configurationAccessor = new SettingsProxyConfigAccessor(encryptionUtility, settingsProxyConfigurationRepository, nonProxyHostsConfigurationRepository);
        SettingsProxyCrudActions settingsProxyCrudActions = new SettingsProxyCrudActions(authorizationManager, configurationAccessor, validator, settingsDescriptorKey);
        ProxyConfigurationModelSaveActions saveActions = new ProxyConfigurationModelSaveActions(converter, settingsProxyCrudActions);
        String newHost = "updatedHost";
        ConfigurationModel configurationModel = createDefaultConfigurationModel();

        saveActions.createConcreteModel(configurationModel);

        updateField(configurationModel, ProxyConfigurationModelConverter.FIELD_KEY_HOST, newHost);
        updateField(configurationModel, ProxyConfigurationModelConverter.FIELD_KEY_PORT, "badPort");

        saveActions.updateConcreteModel(configurationModel);

        // Assert that the update failed and the original created values are used.
        SettingsProxyConfigurationEntity actualEntity = savedEntity.get();
        assertEquals(TEST_PROXY_HOST, actualEntity.getHost());
        assertEquals(Integer.valueOf(TEST_PROXY_PORT), actualEntity.getPort());
        assertEquals(TEST_PROXY_USERNAME, actualEntity.getUsername());
        assertEquals(TEST_PROXY_PASSWORD, encryptionUtility.decrypt(actualEntity.getPassword()));
    }

    @Test
    void deleteTest() {
        AtomicReference<SettingsProxyConfigurationEntity> savedEntity = new AtomicReference<>();
        AtomicReference<NonProxyHostConfigurationEntity> savedNonProxyHostEntity = new AtomicReference<>();
        SettingsProxyConfigurationRepository settingsProxyConfigurationRepository = Mockito.mock(SettingsProxyConfigurationRepository.class);
        NonProxyHostsConfigurationRepository nonProxyHostsConfigurationRepository = Mockito.mock(NonProxyHostsConfigurationRepository.class);
        Mockito.when(settingsProxyConfigurationRepository.save(Mockito.any(SettingsProxyConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        Mockito.when(settingsProxyConfigurationRepository.findByName(Mockito.anyString())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));
        Mockito.doAnswer(invocation -> {
            savedEntity.set(null);
            savedNonProxyHostEntity.set(null);
            return null;
        }).when(settingsProxyConfigurationRepository).deleteByName(Mockito.anyString());

        Mockito.when(nonProxyHostsConfigurationRepository.saveAll(Mockito.any(List.class))).thenAnswer(invocation -> {
            Iterable<NonProxyHostConfigurationEntity> iterable = invocation.getArgument(0);
            for (NonProxyHostConfigurationEntity entity : iterable) {
                savedNonProxyHostEntity.set(entity);
            }
            return List.of(savedNonProxyHostEntity.get());
        });

        SettingsProxyConfigAccessor configurationAccessor = new SettingsProxyConfigAccessor(encryptionUtility, settingsProxyConfigurationRepository, nonProxyHostsConfigurationRepository);
        SettingsProxyCrudActions settingsProxyCrudActions = new SettingsProxyCrudActions(authorizationManager, configurationAccessor, validator, settingsDescriptorKey);
        ProxyConfigurationModelSaveActions saveActions = new ProxyConfigurationModelSaveActions(converter, settingsProxyCrudActions);
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        saveActions.createConcreteModel(configurationModel);

        SettingsProxyConfigurationEntity actualEntity = savedEntity.get();
        NonProxyHostConfigurationEntity actualNonProxyHosts = savedNonProxyHostEntity.get();
        assertNotNull(actualEntity);
        assertNotNull(actualNonProxyHosts);

        saveActions.deleteConcreteModel(configurationModel);
        actualEntity = savedEntity.get();
        actualNonProxyHosts = savedNonProxyHostEntity.get();
        assertNull(actualEntity);
        assertNull(actualNonProxyHosts);
    }

    private AuthorizationManager createAuthorizationManager() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), settingsDescriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    private ConfigurationModel createDefaultConfigurationModel() {
        Map<String, ConfigurationFieldModel> fieldValuesMap = new HashMap<>();

        ConfigurationFieldModel hostField = ConfigurationFieldModel.create(ProxyConfigurationModelConverter.FIELD_KEY_HOST);
        ConfigurationFieldModel portField = ConfigurationFieldModel.create(ProxyConfigurationModelConverter.FIELD_KEY_PORT);
        ConfigurationFieldModel usernameField = ConfigurationFieldModel.create(ProxyConfigurationModelConverter.FIELD_KEY_USERNAME);
        ConfigurationFieldModel passwordField = ConfigurationFieldModel.create(ProxyConfigurationModelConverter.FIELD_KEY_PASSWORD);

        ConfigurationFieldModel nonProxyHostField = ConfigurationFieldModel.create(ProxyConfigurationModelConverter.FIELD_KEY_NON_PROXY_HOSTS);

        hostField.setFieldValue(TEST_PROXY_HOST);
        portField.setFieldValue(TEST_PROXY_PORT);
        usernameField.setFieldValue(TEST_PROXY_USERNAME);
        passwordField.setFieldValue(TEST_PROXY_PASSWORD);
        nonProxyHostField.setFieldValue(TEST_PROXY_NON_PROXY_HOST);
        fieldValuesMap.put(ProxyConfigurationModelConverter.FIELD_KEY_HOST, hostField);
        fieldValuesMap.put(ProxyConfigurationModelConverter.FIELD_KEY_PORT, portField);
        fieldValuesMap.put(ProxyConfigurationModelConverter.FIELD_KEY_USERNAME, usernameField);
        fieldValuesMap.put(ProxyConfigurationModelConverter.FIELD_KEY_PASSWORD, passwordField);
        fieldValuesMap.put(ProxyConfigurationModelConverter.FIELD_KEY_NON_PROXY_HOSTS, nonProxyHostField);

        return new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, fieldValuesMap);
    }

    private void updateField(ConfigurationModel configurationModel, String key, String value) {
        configurationModel.getField(key).ifPresent(field -> field.setFieldValue(value));
    }
}
