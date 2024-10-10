package com.blackduck.integration.alert.channel.jira.server.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.channel.jira.server.action.JiraServerGlobalCrudActions;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationEntity;
import com.blackduck.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationRepository;
import com.blackduck.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

class JiraServerGlobalConfigurationModelSaveActionsTest {
    public static final String TEST_URL = "https://test.jira.example.com";
    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_PASSWORD = "testpassword";
    public static final String TEST_DISABLE_PLUGIN_CHECK = "true";

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
    private final AuthorizationManager authorizationManager = createAuthorizationManager();

    private JiraServerGlobalConfigurationModelConverter converter;

    @BeforeEach
    public void init() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        converter = new JiraServerGlobalConfigurationModelConverter(validator);
    }

    @Test
    void getDescriptorKeyTest() {
        JiraServerGlobalConfigurationModelSaveActions saveActions = new JiraServerGlobalConfigurationModelSaveActions(null, null, null);
        assertEquals(ChannelKeys.JIRA_SERVER, saveActions.getDescriptorKey());
    }

    @Test
    void createTest() {
        AtomicReference<JiraServerConfigurationEntity> savedEntity = new AtomicReference<>();
        JiraServerConfigurationRepository jiraConfigurationRepository = Mockito.mock(JiraServerConfigurationRepository.class);
        Mockito.when(jiraConfigurationRepository.save(Mockito.any(JiraServerConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        JiraServerGlobalConfigAccessor configurationAccessor = new JiraServerGlobalConfigAccessor(encryptionUtility, jiraConfigurationRepository);
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configurationAccessor, createValidator());
        JiraServerGlobalConfigurationModelSaveActions saveActions = new JiraServerGlobalConfigurationModelSaveActions(converter, crudActions, configurationAccessor);
        saveActions.createConcreteModel(createDefaultConfigurationModel());

        JiraServerConfigurationEntity actualEntity = savedEntity.get();
        assertEquals(TEST_URL, actualEntity.getUrl());
        assertEquals(TEST_USERNAME, actualEntity.getUsername());
        assertEquals(TEST_PASSWORD, encryptionUtility.decrypt(actualEntity.getPassword()));
        assertTrue(actualEntity.getDisablePluginCheck());
    }

    @Test
    void createInvalidConversionTest() {
        AtomicReference<JiraServerConfigurationEntity> savedEntity = new AtomicReference<>();
        JiraServerConfigurationRepository jiraConfigurationRepository = Mockito.mock(JiraServerConfigurationRepository.class);
        Mockito.when(jiraConfigurationRepository.save(Mockito.any(JiraServerConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        JiraServerGlobalConfigAccessor configurationAccessor = new JiraServerGlobalConfigAccessor(encryptionUtility, jiraConfigurationRepository);
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configurationAccessor, createValidator());
        JiraServerGlobalConfigurationModelSaveActions saveActions = new JiraServerGlobalConfigurationModelSaveActions(converter, crudActions, configurationAccessor);
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        updateField(configurationModel, JiraServerGlobalConfigurationModelConverter.URL_KEY, "      ");
        saveActions.createConcreteModel(configurationModel);

        JiraServerConfigurationEntity actualEntity = savedEntity.get();
        assertNull(actualEntity);
    }

    @Test
    void updateTest() {
        AtomicReference<JiraServerConfigurationEntity> savedEntity = new AtomicReference<>();
        JiraServerConfigurationRepository jiraConfigurationRepository = Mockito.mock(JiraServerConfigurationRepository.class);
        Mockito.when(jiraConfigurationRepository.save(Mockito.any(JiraServerConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        Mockito.when(jiraConfigurationRepository.findByName(Mockito.anyString())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));
        Mockito.when(jiraConfigurationRepository.findById(Mockito.any())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));
        Mockito.when(jiraConfigurationRepository.existsByConfigurationId(Mockito.any(UUID.class))).thenAnswer(invocation -> savedEntity.get() != null);

        JiraServerGlobalConfigAccessor configurationAccessor = new JiraServerGlobalConfigAccessor(encryptionUtility, jiraConfigurationRepository);
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configurationAccessor, createValidator());
        JiraServerGlobalConfigurationModelSaveActions saveActions = new JiraServerGlobalConfigurationModelSaveActions(converter, crudActions, configurationAccessor);
        String newPassword = "updatedPassword";
        String newUrl = "https://updated.jira.example.com";
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        saveActions.createConcreteModel(configurationModel);

        updateField(configurationModel, JiraServerGlobalConfigurationModelConverter.URL_KEY, newUrl);
        updateField(configurationModel, JiraServerGlobalConfigurationModelConverter.PASSWORD_KEY, newPassword);

        saveActions.updateConcreteModel(configurationModel);

        JiraServerConfigurationEntity actualEntity = savedEntity.get();
        assertEquals(newUrl, actualEntity.getUrl());
        assertEquals(TEST_USERNAME, actualEntity.getUsername());
        assertEquals(newPassword, encryptionUtility.decrypt(actualEntity.getPassword()));
        assertTrue(actualEntity.getDisablePluginCheck());
    }

    @Test
    void updateInvalidConversionTest() {
        AtomicReference<JiraServerConfigurationEntity> savedEntity = new AtomicReference<>();
        JiraServerConfigurationRepository jiraConfigurationRepository = Mockito.mock(JiraServerConfigurationRepository.class);
        Mockito.when(jiraConfigurationRepository.save(Mockito.any(JiraServerConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        Mockito.when(jiraConfigurationRepository.findByName(Mockito.anyString())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));
        Mockito.when(jiraConfigurationRepository.findById(Mockito.any())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));
        Mockito.when(jiraConfigurationRepository.existsByConfigurationId(Mockito.any(UUID.class))).thenAnswer(invocation -> savedEntity.get() != null);

        JiraServerGlobalConfigAccessor configurationAccessor = new JiraServerGlobalConfigAccessor(encryptionUtility, jiraConfigurationRepository);
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configurationAccessor, createValidator());
        JiraServerGlobalConfigurationModelSaveActions saveActions = new JiraServerGlobalConfigurationModelSaveActions(converter, crudActions, configurationAccessor);
        String newPassword = "updatedPassword";
        String invalidUrl = "      \t\r\n       ";
        ConfigurationModel configurationModel = createDefaultConfigurationModel();

        saveActions.createConcreteModel(configurationModel);

        updateField(configurationModel, JiraServerGlobalConfigurationModelConverter.URL_KEY, invalidUrl);
        updateField(configurationModel, JiraServerGlobalConfigurationModelConverter.PASSWORD_KEY, newPassword);

        saveActions.updateConcreteModel(configurationModel);

        // make sure the values are not the updated values
        JiraServerConfigurationEntity actualEntity = savedEntity.get();
        assertEquals(TEST_URL, actualEntity.getUrl());
        assertEquals(TEST_USERNAME, actualEntity.getUsername());
        assertEquals(TEST_PASSWORD, encryptionUtility.decrypt(actualEntity.getPassword()));
        assertTrue(actualEntity.getDisablePluginCheck());
    }

    @Test
    void updateItemNotFoundUpdateTest() {
        AtomicReference<JiraServerConfigurationEntity> savedEntity = new AtomicReference<>();
        JiraServerConfigurationRepository jiraConfigurationRepository = Mockito.mock(JiraServerConfigurationRepository.class);
        Mockito.when(jiraConfigurationRepository.save(Mockito.any(JiraServerConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        Mockito.when(jiraConfigurationRepository.findByName(Mockito.anyString())).thenAnswer(invocation -> Optional.empty());
        Mockito.when(jiraConfigurationRepository.findById(Mockito.any())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));
        Mockito.when(jiraConfigurationRepository.existsByConfigurationId(Mockito.any(UUID.class))).thenAnswer(invocation -> savedEntity.get() != null);

        JiraServerGlobalConfigAccessor configurationAccessor = new JiraServerGlobalConfigAccessor(encryptionUtility, jiraConfigurationRepository);
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configurationAccessor, createValidator());
        JiraServerGlobalConfigurationModelSaveActions saveActions = new JiraServerGlobalConfigurationModelSaveActions(converter, crudActions, configurationAccessor);
        String newPassword = "updatedPassword";
        String newUrl = "https://updated.jira.example.com";
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        saveActions.createConcreteModel(configurationModel);

        updateField(configurationModel, JiraServerGlobalConfigurationModelConverter.URL_KEY, newUrl);
        updateField(configurationModel, JiraServerGlobalConfigurationModelConverter.PASSWORD_KEY, newPassword);
        saveActions.updateConcreteModel(configurationModel);

        // make sure the values are not the updated values
        JiraServerConfigurationEntity actualEntity = savedEntity.get();
        assertEquals(newUrl, actualEntity.getUrl());
        assertEquals(TEST_USERNAME, actualEntity.getUsername());
        assertEquals(newPassword, encryptionUtility.decrypt(actualEntity.getPassword()));
        assertTrue(actualEntity.getDisablePluginCheck());
    }

    @Test
    void deleteTest() {
        AtomicReference<JiraServerConfigurationEntity> savedEntity = new AtomicReference<>();
        JiraServerConfigurationRepository jiraConfigurationRepository = Mockito.mock(JiraServerConfigurationRepository.class);
        Mockito.when(jiraConfigurationRepository.save(Mockito.any(JiraServerConfigurationEntity.class))).thenAnswer(invocation -> {
            savedEntity.set(invocation.getArgument(0));
            return savedEntity.get();
        });

        Mockito.when(jiraConfigurationRepository.findByName(Mockito.anyString())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));
        Mockito.when(jiraConfigurationRepository.findById(Mockito.any())).thenAnswer(invocation -> Optional.ofNullable(savedEntity.get()));
        Mockito.when(jiraConfigurationRepository.existsByConfigurationId(Mockito.any(UUID.class))).thenAnswer(invocation -> savedEntity.get() != null);
        Mockito.doAnswer(invocation -> {
            savedEntity.set(null);
            return null;
        }).when(jiraConfigurationRepository).deleteById(Mockito.any());

        JiraServerGlobalConfigAccessor configurationAccessor = new JiraServerGlobalConfigAccessor(encryptionUtility, jiraConfigurationRepository);
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configurationAccessor, createValidator());
        JiraServerGlobalConfigurationModelSaveActions saveActions = new JiraServerGlobalConfigurationModelSaveActions(converter, crudActions, configurationAccessor);
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        saveActions.createConcreteModel(configurationModel);
        JiraServerConfigurationEntity actualEntity = savedEntity.get();
        assertNotNull(actualEntity);
        saveActions.deleteConcreteModel(configurationModel);

        actualEntity = savedEntity.get();
        assertNull(actualEntity);
    }

    private JiraServerGlobalConfigurationValidator createValidator() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        return new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
    }

    private AuthorizationManager createAuthorizationManager() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.JIRA_SERVER;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    private ConfigurationModel createDefaultConfigurationModel() {
        Map<String, ConfigurationFieldModel> fieldValuesMap = new HashMap<>();

        ConfigurationFieldModel urlField = ConfigurationFieldModel.create(JiraServerGlobalConfigurationModelConverter.URL_KEY);
        ConfigurationFieldModel userField = ConfigurationFieldModel.create(JiraServerGlobalConfigurationModelConverter.USERNAME_KEY);
        ConfigurationFieldModel passwordField = ConfigurationFieldModel.create(JiraServerGlobalConfigurationModelConverter.PASSWORD_KEY);
        ConfigurationFieldModel disableCheckField = ConfigurationFieldModel.create(JiraServerGlobalConfigurationModelConverter.DISABLE_PLUGIN_CHECK_KEY);

        urlField.setFieldValue(TEST_URL);
        passwordField.setFieldValue(TEST_PASSWORD);
        userField.setFieldValue(TEST_USERNAME);
        disableCheckField.setFieldValue(TEST_DISABLE_PLUGIN_CHECK);
        fieldValuesMap.put(JiraServerGlobalConfigurationModelConverter.URL_KEY, urlField);
        fieldValuesMap.put(JiraServerGlobalConfigurationModelConverter.PASSWORD_KEY, passwordField);
        fieldValuesMap.put(JiraServerGlobalConfigurationModelConverter.USERNAME_KEY, userField);
        fieldValuesMap.put(JiraServerGlobalConfigurationModelConverter.DISABLE_PLUGIN_CHECK_KEY, disableCheckField);
        return new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, fieldValuesMap);
    }

    private void updateField(ConfigurationModel configurationModel, String key, String value) {
        configurationModel.getField(key).ifPresent(field -> field.setFieldValue(value));
    }
}
