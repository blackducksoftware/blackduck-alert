package com.synopsys.integration.alert.channel.azure.boards.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.azure.boards.action.AzureBoardsGlobalCrudActions;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.database.configuration.AzureBoardsConfigurationEntity;
import com.synopsys.integration.alert.channel.azure.boards.database.mock.MockAzureBoardsConfigurationRepository;
import com.synopsys.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.alert.api.descriptor.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.database.MockRepositorySorter;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

class AzureBoardsGlobalConfigurationModelSaveActionsTest {
    private static final String TEST_ORGANIZATION_NAME = "testOrganizationName";
    private static final String TEST_CLIENT_ID = "testClientID";
    private static final String TEST_CLIENT_SECRET = "testClientSecret";
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);
    private final AuthorizationManager authorizationManager = createAuthorizationManager();
    private MockAzureBoardsConfigurationRepository mockAzureRepository;
    private AzureBoardsGlobalConfigAccessor configAccessor;
    private AzureBoardsGlobalCrudActions crudActions;
    private AzureBoardsGlobalConfigurationValidator validator;
    private AzureBoardsGlobalConfigurationModelConverter converter;

    @BeforeEach
    public void init() {
        MockRepositorySorter<AzureBoardsConfigurationEntity> sorter = new MockRepositorySorter<>();
        sorter.applyFieldSorter("name", MockRepositorySorter.createSingleFieldSorter(AzureBoardsConfigurationEntity::getName));
        sorter.applyFieldSorter("organizationName", MockRepositorySorter.createSingleFieldSorter(AzureBoardsConfigurationEntity::getOrganizationName));
        mockAzureRepository = new MockAzureBoardsConfigurationRepository(sorter);
        configAccessor = new AzureBoardsGlobalConfigAccessor(encryptionUtility, mockAzureRepository);
        validator = new AzureBoardsGlobalConfigurationValidator(configAccessor);
        crudActions = new AzureBoardsGlobalCrudActions(authorizationManager, configAccessor, validator);
        converter = new AzureBoardsGlobalConfigurationModelConverter(validator);
    }

    @Test
    void getDescriptorKeyTest() {
        AzureBoardsGlobalConfigurationModelSaveActions saveActions = new AzureBoardsGlobalConfigurationModelSaveActions(null, null, null);
        assertEquals(ChannelKeys.AZURE_BOARDS, saveActions.getDescriptorKey());
    }

    @Test
    void createTest() {
        AzureBoardsGlobalConfigurationModelSaveActions saveActions = new AzureBoardsGlobalConfigurationModelSaveActions(
            converter,
            crudActions,
            configAccessor
        );
        saveActions.createConcreteModel(createDefaultConfigurationModel());
        Optional<AzureBoardsConfigurationEntity> entityOptional = mockAzureRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(entityOptional.isPresent());
        AzureBoardsConfigurationEntity entity = entityOptional.get();
        assertEquals(TEST_ORGANIZATION_NAME, entity.getOrganizationName());
        assertEquals(TEST_CLIENT_ID, encryptionUtility.decrypt(entity.getAppId()));
        assertEquals(TEST_CLIENT_SECRET, encryptionUtility.decrypt(entity.getClientSecret()));
    }

    @Test
    void createInvalidConversionTest() {
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        configurationModel.getField(AzureBoardsGlobalConfigurationModelConverter.ORGANIZATION_NAME).ifPresent(field -> field.setFieldValue("  "));
        AzureBoardsGlobalConfigurationModelSaveActions saveActions = new AzureBoardsGlobalConfigurationModelSaveActions(
            converter,
            crudActions,
            configAccessor
        );
        saveActions.createConcreteModel(configurationModel);
        Optional<AzureBoardsConfigurationEntity> entityOptional = mockAzureRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(entityOptional.isEmpty());
    }

    @Test
    @Disabled("Blocked by IALERT-3195")
    void updateTest() {
        AzureBoardsGlobalConfigurationModelSaveActions saveActions = new AzureBoardsGlobalConfigurationModelSaveActions(
            converter,
            crudActions,
            configAccessor
        );
        saveActions.createConcreteModel(createDefaultConfigurationModel());
        Optional<AzureBoardsConfigurationEntity> entityOptional = mockAzureRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(entityOptional.isPresent());

        String newAppId = "updatedAppId";
        String newClientSecret = "updatedClientSecret";
        ConfigurationModel updatedConfigurationModel = createDefaultConfigurationModel();
        updatedConfigurationModel.getField(AzureBoardsGlobalConfigurationModelConverter.CLIENT_ID).ifPresent(field -> field.setFieldValue(newAppId));
        updatedConfigurationModel.getField(AzureBoardsGlobalConfigurationModelConverter.CLIENT_SECRET).ifPresent(field -> field.setFieldValue(newClientSecret));
        saveActions.updateConcreteModel(updatedConfigurationModel);

        Optional<AzureBoardsConfigurationEntity> updatedEntityOptional = mockAzureRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(updatedEntityOptional.isPresent());
        AzureBoardsConfigurationEntity updatedEntity = updatedEntityOptional.get();
        assertEquals(TEST_ORGANIZATION_NAME, updatedEntity.getOrganizationName());
        assertEquals(newAppId, encryptionUtility.decrypt(updatedEntity.getAppId()));
        assertEquals(newClientSecret, encryptionUtility.decrypt(updatedEntity.getClientSecret()));
    }

    @Test
    @Disabled("Blocked by IALERT-3195")
    void updateInvalidConversionTest() {
        //TODO: Implement after resolving IALERT-3195
    }

    @Test
    @Disabled("Blocked by IALERT-3195")
    void updateItemNotFoundTest() {
        //TODO: Implement after resolving IALERT-3195
    }

    @Test
    void deleteTest() {
        AzureBoardsGlobalConfigurationModelSaveActions saveActions = new AzureBoardsGlobalConfigurationModelSaveActions(
            converter,
            crudActions,
            configAccessor
        );
        saveActions.createConcreteModel(createDefaultConfigurationModel());
        Optional<AzureBoardsConfigurationEntity> entityOptional = mockAzureRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(entityOptional.isPresent());

        saveActions.deleteConcreteModel(createDefaultConfigurationModel());
        entityOptional = mockAzureRepository.findByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(entityOptional.isEmpty());
    }

    private AuthorizationManager createAuthorizationManager() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.AZURE_BOARDS;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    private ConfigurationModel createDefaultConfigurationModel() {
        Map<String, ConfigurationFieldModel> fieldValuesMap = new HashMap<>();

        ConfigurationFieldModel organizationNameField = ConfigurationFieldModel.create(AzureBoardsGlobalConfigurationModelConverter.ORGANIZATION_NAME);
        ConfigurationFieldModel clientIdField = ConfigurationFieldModel.create(AzureBoardsGlobalConfigurationModelConverter.CLIENT_ID);
        ConfigurationFieldModel clientSecretField = ConfigurationFieldModel.create(AzureBoardsGlobalConfigurationModelConverter.CLIENT_SECRET);

        organizationNameField.setFieldValue(TEST_ORGANIZATION_NAME);
        clientIdField.setFieldValue(TEST_CLIENT_ID);
        clientSecretField.setFieldValue(TEST_CLIENT_SECRET);
        fieldValuesMap.put(AzureBoardsGlobalConfigurationModelConverter.ORGANIZATION_NAME, organizationNameField);
        fieldValuesMap.put(AzureBoardsGlobalConfigurationModelConverter.CLIENT_ID, clientIdField);
        fieldValuesMap.put(AzureBoardsGlobalConfigurationModelConverter.CLIENT_SECRET, clientSecretField);
        return new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, fieldValuesMap);
    }
}
