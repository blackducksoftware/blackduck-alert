package com.blackduck.integration.alert.channel.azure.boards.database.accessor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.channel.azure.boards.database.configuration.AzureBoardsConfigurationEntity;
import com.blackduck.integration.alert.channel.azure.boards.database.mock.MockAzureBoardsConfigurationRepository;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.alert.test.common.database.MockRepositorySorter;

class AzureBoardsGlobalConfigAccessorTest {
    private static final String TEST_ORG_NAME = "organizationName";
    private static final String TEST_APP_ID = "appId-test-value";
    private static final String TEST_CLIENT_SECRET = "clientSecret-test-value";

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

    private MockAzureBoardsConfigurationRepository mockAzureBoardsConfigurationRepository;
    private AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;

    @BeforeEach
    public void init() {
        MockRepositorySorter<AzureBoardsConfigurationEntity> sorter = new MockRepositorySorter<>();
        sorter.applyFieldSorter("name", MockRepositorySorter.createSingleFieldSorter(AzureBoardsConfigurationEntity::getName));
        sorter.applyFieldSorter("organizationName", MockRepositorySorter.createSingleFieldSorter(AzureBoardsConfigurationEntity::getOrganizationName));
        mockAzureBoardsConfigurationRepository = new MockAzureBoardsConfigurationRepository(sorter);
        azureBoardsGlobalConfigAccessor = new AzureBoardsGlobalConfigAccessor(encryptionUtility, mockAzureBoardsConfigurationRepository);
    }

    @Test
    void configurationCountTest() {
        assertEquals(0, azureBoardsGlobalConfigAccessor.getConfigurationCount());
        mockAzureBoardsConfigurationRepository.save(createEntity(UUID.randomUUID()));
        assertEquals(1, azureBoardsGlobalConfigAccessor.getConfigurationCount());
    }

    @Test
    void getByConfigurationIdTest() {
        UUID id = UUID.randomUUID();
        AzureBoardsConfigurationEntity entity = createEntity(id);
        mockAzureBoardsConfigurationRepository.save(entity);
        Optional<AzureBoardsGlobalConfigModel> configModelOptional = azureBoardsGlobalConfigAccessor.getConfiguration(id);
        assertTrue(configModelOptional.isPresent());
        validateModel(entity, configModelOptional.get());
    }

    @Test
    void getByConfigurationIdNotFoundTest() {
        UUID id = UUID.randomUUID();
        AzureBoardsConfigurationEntity entity = createEntity(id);
        mockAzureBoardsConfigurationRepository.save(entity);
        Optional<AzureBoardsGlobalConfigModel> configModelOptional = azureBoardsGlobalConfigAccessor.getConfiguration(UUID.randomUUID());
        assertTrue(configModelOptional.isEmpty());
    }

    @Test
    void getConfigurationByNameTest() {
        UUID id = UUID.randomUUID();
        AzureBoardsConfigurationEntity entity = createEntity(id);
        mockAzureBoardsConfigurationRepository.save(entity);
        Optional<AzureBoardsGlobalConfigModel> configModelOptional = azureBoardsGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(configModelOptional.isPresent());
        validateModel(entity, configModelOptional.get());
    }

    @Test
    void getConfigurationByNameNotFoundTest() {
        UUID id = UUID.randomUUID();
        AzureBoardsConfigurationEntity entity = createEntity(id, "myAzureProjectName", OffsetDateTime.now(), OffsetDateTime.now());
        mockAzureBoardsConfigurationRepository.save(entity);
        Optional<AzureBoardsGlobalConfigModel> configModelOptional = azureBoardsGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        assertTrue(configModelOptional.isEmpty());
    }

    @Test
    void getPageTest() {
        UUID id = UUID.randomUUID();
        AzureBoardsConfigurationEntity entity = createEntity(id);
        mockAzureBoardsConfigurationRepository.save(entity);
        AlertPagedModel<AzureBoardsGlobalConfigModel> pagedModel = azureBoardsGlobalConfigAccessor.getConfigurationPage(0, 10, null, null, null);
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertNotNull(pagedModel.getModels());
        assertEquals(1, pagedModel.getModels().size());
        AzureBoardsGlobalConfigModel configModel = pagedModel.getModels().get(0);
        validateModel(entity, configModel);
    }

    @Test
    void getPageWithSearchTermTest() {
        UUID id = UUID.randomUUID();
        AzureBoardsConfigurationEntity entity = createEntity(id);
        mockAzureBoardsConfigurationRepository.save(entity);
        AlertPagedModel<AzureBoardsGlobalConfigModel> pagedModel = azureBoardsGlobalConfigAccessor.getConfigurationPage(
            0,
            10,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            null,
            null
        );
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertNotNull(pagedModel.getModels());
        assertEquals(1, pagedModel.getModels().size());
        AzureBoardsGlobalConfigModel configModel = pagedModel.getModels().get(0);
        validateModel(entity, configModel);
    }

    @Test
    void getPageSortAscendingTest() {
        UUID id = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        AzureBoardsConfigurationEntity entityZ = createEntity(id, "Z-project", OffsetDateTime.now(), OffsetDateTime.now());
        AzureBoardsConfigurationEntity entityB = createEntity(id2, "B-project", OffsetDateTime.now(), OffsetDateTime.now());
        AzureBoardsConfigurationEntity entityA = createEntity(UUID.randomUUID(), "A-project", OffsetDateTime.now(), OffsetDateTime.now());
        AzureBoardsConfigurationEntity entityC = createEntity(UUID.randomUUID(), "C-project", OffsetDateTime.now(), OffsetDateTime.now());
        mockAzureBoardsConfigurationRepository.saveAll(List.of(entityZ, entityB, entityA, entityC));
        AlertPagedModel<AzureBoardsGlobalConfigModel> pagedModel = azureBoardsGlobalConfigAccessor.getConfigurationPage(
            0,
            10,
            "",
            "name",
            "asc"
        );
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertNotNull(pagedModel.getModels());
        List<AzureBoardsGlobalConfigModel> models = pagedModel.getModels();
        assertEquals(4, models.size());
        assertEquals(entityA.getName(), models.get(0).getName());
        assertEquals(entityB.getName(), models.get(1).getName());
        assertEquals(entityC.getName(), models.get(2).getName());
        assertEquals(entityZ.getName(), models.get(3).getName());
    }

    @Test
    void getPageSortDescendingTest() {
        UUID id = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        AzureBoardsConfigurationEntity entityZ = createEntity(id, "Z-project", OffsetDateTime.now(), OffsetDateTime.now());
        AzureBoardsConfigurationEntity entityB = createEntity(id2, "B-project", OffsetDateTime.now(), OffsetDateTime.now());
        AzureBoardsConfigurationEntity entityA = createEntity(UUID.randomUUID(), "A-project", OffsetDateTime.now(), OffsetDateTime.now());
        AzureBoardsConfigurationEntity entityC = createEntity(UUID.randomUUID(), "C-project", OffsetDateTime.now(), OffsetDateTime.now());
        mockAzureBoardsConfigurationRepository.saveAll(List.of(entityZ, entityB, entityA, entityC));
        AlertPagedModel<AzureBoardsGlobalConfigModel> pagedModel = azureBoardsGlobalConfigAccessor.getConfigurationPage(
            0,
            10,
            "",
            "name",
            "desc"
        );
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertNotNull(pagedModel.getModels());
        List<AzureBoardsGlobalConfigModel> models = pagedModel.getModels();
        assertEquals(4, models.size());
        assertEquals(entityZ.getName(), models.get(0).getName());
        assertEquals(entityC.getName(), models.get(1).getName());
        assertEquals(entityB.getName(), models.get(2).getName());
        assertEquals(entityA.getName(), models.get(3).getName());
    }

    @Test
    void getPageEmptyTest() {
        UUID id = UUID.randomUUID();
        AzureBoardsConfigurationEntity entity = createEntity(id);
        mockAzureBoardsConfigurationRepository.save(entity);
        AlertPagedModel<AzureBoardsGlobalConfigModel> pagedModel = azureBoardsGlobalConfigAccessor.getConfigurationPage(
            0,
            10,
            "projectDoesntExist",
            null,
            null
        );
        assertEquals(0, pagedModel.getCurrentPage());
        assertEquals(1, pagedModel.getTotalPages());
        assertEquals(0, pagedModel.getModels().size());
    }

    @Test
    void createConfigurationTest() {
        AzureBoardsGlobalConfigModel modelToCreate = new AzureBoardsGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            TEST_ORG_NAME,
            TEST_APP_ID,
            TEST_CLIENT_SECRET
        );
        AzureBoardsGlobalConfigModel createdModel = assertDoesNotThrow(() -> azureBoardsGlobalConfigAccessor.createConfiguration(modelToCreate));
        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, createdModel.getName());
        assertTrue(createdModel.getIsAppIdSet().orElse(Boolean.FALSE));
        assertTrue(createdModel.getIsClientSecretSet().orElse(Boolean.FALSE));
        assertEquals(TEST_APP_ID, createdModel.getAppId().orElse(null));
        assertEquals(TEST_CLIENT_SECRET, createdModel.getClientSecret().orElse(null));
    }

    @Test
    void updateConfigurationTest() {
        UUID id = UUID.randomUUID();
        String updatedName = "updatedName";
        String newOrgName = "newOrgName";
        AzureBoardsConfigurationEntity entity = createEntity(id);
        mockAzureBoardsConfigurationRepository.save(entity);
        AzureBoardsGlobalConfigModel modelToUpdate = new AzureBoardsGlobalConfigModel(
            id.toString(),
            updatedName,
            newOrgName,
            TEST_APP_ID,
            TEST_CLIENT_SECRET
        );
        AzureBoardsGlobalConfigModel updatedModel = assertDoesNotThrow(() -> azureBoardsGlobalConfigAccessor.updateConfiguration(id, modelToUpdate));
        assertEquals(id.toString(), updatedModel.getId());
        assertEquals(updatedName, updatedModel.getName());
        assertEquals(newOrgName, updatedModel.getOrganizationName());
        assertEquals(TEST_APP_ID, updatedModel.getAppId().orElse(null));
        assertEquals(TEST_CLIENT_SECRET, updatedModel.getClientSecret().orElse(null));
    }

    /**
     * appId/ClientId are set to default values for saved entity and updated model has both set to null.
     * Expected model should have fields with values from the original entity.
     * Updated model must have isAppIdSet and isClientSecretSet both set to True.
     */
    @Test
    void updateSensitiveFieldsAreSavedTest() {
        UUID id = UUID.randomUUID();
        String updatedName = "updatedName";
        String newOrgName = "newOrgName";
        AzureBoardsConfigurationEntity entity = createEntity(id);
        mockAzureBoardsConfigurationRepository.save(entity);
        AzureBoardsGlobalConfigModel modelToUpdate = new AzureBoardsGlobalConfigModel(
            id.toString(),
            updatedName,
            OffsetDateTime.now().toString(),
            OffsetDateTime.now().toString(),
            newOrgName,
            null,
            true,
            null,
            true
        );
        AzureBoardsGlobalConfigModel updatedModel = assertDoesNotThrow(() -> azureBoardsGlobalConfigAccessor.updateConfiguration(id, modelToUpdate));
        assertEquals(id.toString(), updatedModel.getId());
        assertEquals(updatedName, updatedModel.getName());
        assertEquals(newOrgName, updatedModel.getOrganizationName());
        assertEquals(TEST_APP_ID, updatedModel.getAppId().orElse(null));
        assertEquals(TEST_CLIENT_SECRET, updatedModel.getClientSecret().orElse(null));
    }

    @Test
    void updateConfigurationDoesNotExistTest() {
        UUID id = UUID.randomUUID();
        String updatedName = "updatedName";
        String newOrgName = "newOrgName";
        AzureBoardsGlobalConfigModel modelToUpdate = new AzureBoardsGlobalConfigModel(
            id.toString(),
            updatedName,
            newOrgName,
            TEST_APP_ID,
            TEST_CLIENT_SECRET
        );
        assertThrows(AlertConfigurationException.class, () -> azureBoardsGlobalConfigAccessor.updateConfiguration(id, modelToUpdate));
    }

    @Test
    void deleteConfigurationTest() {
        UUID id = UUID.randomUUID();
        AzureBoardsConfigurationEntity entity = createEntity(id);
        mockAzureBoardsConfigurationRepository.save(entity);
        assertTrue(mockAzureBoardsConfigurationRepository.findById(id).isPresent());
        azureBoardsGlobalConfigAccessor.deleteConfiguration(id);
        assertFalse(mockAzureBoardsConfigurationRepository.findById(id).isPresent());
    }

    @Test
    void deleteConfigurationNullTest() {
        UUID id = UUID.randomUUID();
        AzureBoardsConfigurationEntity entity = createEntity(id);
        mockAzureBoardsConfigurationRepository.save(entity);
        assertTrue(mockAzureBoardsConfigurationRepository.findById(id).isPresent());
        azureBoardsGlobalConfigAccessor.deleteConfiguration(null);
        assertTrue(mockAzureBoardsConfigurationRepository.findById(id).isPresent());
    }

    private AzureBoardsConfigurationEntity createEntity(UUID id) {
        return createEntity(id, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, OffsetDateTime.now(), OffsetDateTime.now());
    }

    private AzureBoardsConfigurationEntity createEntity(UUID id, String name, OffsetDateTime createdAt, OffsetDateTime lastUpdated) {
        return new AzureBoardsConfigurationEntity(
            id,
            name,
            createdAt,
            lastUpdated,
            TEST_ORG_NAME,
            encryptionUtility.encrypt(TEST_APP_ID),
            encryptionUtility.encrypt(TEST_CLIENT_SECRET)
        );
    }

    private void validateModel(AzureBoardsConfigurationEntity entity, AzureBoardsGlobalConfigModel configModel) {
        assertEquals(entity.getConfigurationId().toString(), configModel.getId());
        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, configModel.getName());
        assertEquals(TEST_ORG_NAME, configModel.getOrganizationName());
        assertTrue(configModel.getIsAppIdSet().orElse(Boolean.FALSE));
        assertEquals(TEST_APP_ID, configModel.getAppId().orElse(null));
        assertTrue(configModel.getIsClientSecretSet().orElse(Boolean.FALSE));
        assertEquals(TEST_CLIENT_SECRET, configModel.getClientSecret().orElse(null));
    }
}
