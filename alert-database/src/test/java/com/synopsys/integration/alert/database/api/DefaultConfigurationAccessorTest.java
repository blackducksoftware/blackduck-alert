package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorTypeEntity;
import com.synopsys.integration.alert.database.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;

public class DefaultConfigurationAccessorTest {
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TEST_SALT = "testSalt";
    private static final String TEST_DIRECTORY = "./testDB";
    private static final String TEST_SECRETS_DIRECTORY = "./testDB/run/secrets";

    private AlertProperties alertProperties;
    private FilePersistenceUtil filePersistenceUtil;

    private DescriptorConfigRepository descriptorConfigRepository;
    private ConfigContextRepository configContextRepository;
    private FieldValueRepository fieldValueRepository;
    private DefinedFieldRepository definedFieldRepository;
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    private DescriptorTypeRepository descriptorTypeRepository;
    private EncryptionUtility encryptionUtility;

    private final ConfigContextEnum configContextEnum = ConfigContextEnum.GLOBAL;
    private final String fieldValue = "testFieldValue";
    private final String fieldKey = "channel.common.name";

    @BeforeEach
    public void init() {
        descriptorConfigRepository = Mockito.mock(DescriptorConfigRepository.class);
        configContextRepository = Mockito.mock(ConfigContextRepository.class);
        fieldValueRepository = Mockito.mock(FieldValueRepository.class);
        definedFieldRepository = Mockito.mock(DefinedFieldRepository.class);
        registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
        descriptorTypeRepository = Mockito.mock(DescriptorTypeRepository.class);
        encryptionUtility = createEncryptionUtility();
    }

    @Test
    public void getProviderConfigurationByNameTest() throws Exception {
        final String providerConfigName = "provider-config-name-test";
        final String emptyProviderConfigName = "bad-config-name";
        final Long fieldId = 1L;
        final Long descriptorId = 4L;
        final Long configurationId = 6L;

        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(fieldId);
        FieldValueEntity fieldValueEntity = new FieldValueEntity(2L, 3L, fieldValue);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 5L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(configurationId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());

        Mockito.when(definedFieldRepository.findFirstByKey(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));
        Mockito.when(fieldValueRepository.findAllByFieldIdAndValue(fieldId, providerConfigName)).thenReturn(List.of(fieldValueEntity));
        Mockito.when(fieldValueRepository.findAllByFieldIdAndValue(fieldId, emptyProviderConfigName)).thenReturn(List.of());
        setupGetJobMocks(descriptorConfigEntity, configContextEntity, fieldValueEntity, definedFieldEntity);

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, definedFieldRepository, descriptorConfigRepository, configContextRepository, fieldValueRepository, encryptionUtility);
        Optional<ConfigurationModel> configurationModelOptional = configurationAccessor.getProviderConfigurationByName(providerConfigName);
        Optional<ConfigurationModel> configurationModelProviderConfigsEmpty = configurationAccessor.getProviderConfigurationByName(emptyProviderConfigName);

        assertTrue(configurationModelOptional.isPresent());
        assertFalse(configurationModelProviderConfigsEmpty.isPresent());

        ConfigurationModel configurationModel = configurationModelOptional.get();
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getProviderConfigurationByNameBlankTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        try {
            configurationAccessor.getProviderConfigurationByName("");
            fail("Blank providerConfigName did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getConfigurationByIdEmptyTest() throws Exception {
        Mockito.when(descriptorConfigRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, descriptorConfigRepository, null, null, null);
        Optional<ConfigurationModel> configurationModelOptional = configurationAccessor.getConfigurationById(1L);

        assertFalse(configurationModelOptional.isPresent());
    }

    @Test
    public void getConfigurationByIdNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        try {
            configurationAccessor.getConfigurationById(null);
            fail("Null id did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getConfigurationsByDescriptorKeyTest() throws Exception {
        final Long descriptorId = 3L;
        final Long configurationId = 5L;

        DescriptorKey descriptorKey = createDescriptorKey("descriptorKeyName");
        DescriptorKey badDescriptorKey = createDescriptorKey("bad-descriptorKey");
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name-test", 1L);
        registeredDescriptorEntity.setId(2L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 4L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(configurationId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(6L, 7L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(8L);

        Mockito.when(registeredDescriptorRepository.findFirstByName(descriptorKey.getUniversalKey())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(registeredDescriptorRepository.findFirstByName(badDescriptorKey.getUniversalKey())).thenReturn(Optional.empty());
        setupCreatConfigMocks(descriptorConfigEntity, configContextEntity, fieldValueEntity, definedFieldEntity);

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, null, definedFieldRepository, descriptorConfigRepository, configContextRepository, fieldValueRepository,
            encryptionUtility);
        List<ConfigurationModel> configurationModelList = configurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
        List<ConfigurationModel> configurationModelListEmpty = configurationAccessor.getConfigurationsByDescriptorKey(badDescriptorKey);

        assertEquals(1, configurationModelList.size());
        assertTrue(configurationModelListEmpty.isEmpty());
        ConfigurationModel configurationModel = configurationModelList.get(0);
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getConfigurationsByDescriptorKeyNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        try {
            configurationAccessor.getConfigurationsByDescriptorKey(null);
            fail("Null descriptorKey did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getConfigurationsByDescriptorTypeTest() throws Exception {
        final Long descriptorId = 3L;
        final Long configurationId = 5L;
        DescriptorType descriptorType = DescriptorType.CHANNEL;

        DescriptorTypeEntity descriptorTypeEntity = new DescriptorTypeEntity("CHANNEL");
        descriptorTypeEntity.setId(1L);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name-test", 1L);
        registeredDescriptorEntity.setId(2L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 4L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(configurationId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(6L, 7L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(8L);

        Mockito.when(descriptorTypeRepository.findFirstByType(Mockito.any())).thenReturn(Optional.of(descriptorTypeEntity));
        Mockito.when(registeredDescriptorRepository.findByTypeId(Mockito.any())).thenReturn(List.of(registeredDescriptorEntity));
        setupCreatConfigMocks(descriptorConfigEntity, configContextEntity, fieldValueEntity, definedFieldEntity);

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, descriptorTypeRepository, definedFieldRepository, descriptorConfigRepository, configContextRepository,
            fieldValueRepository,
            encryptionUtility);
        List<ConfigurationModel> configurationModelList = configurationAccessor.getConfigurationsByDescriptorType(descriptorType);

        assertEquals(1, configurationModelList.size());
        ConfigurationModel configurationModel = configurationModelList.get(0);
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getConfigurationsByDescriptorTypeNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        try {
            configurationAccessor.getConfigurationsByDescriptorType(null);
            fail("Null descriptorType did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getChannelConfigurationsByFrequencyTest() throws Exception {
        final Long descriptorId = 3L;
        final Long configurationId = 5L;
        FrequencyType frequencyType = FrequencyType.DAILY;

        DescriptorTypeEntity descriptorTypeEntity = new DescriptorTypeEntity("CHANNEL");
        descriptorTypeEntity.setId(1L);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name-test", 1L);
        registeredDescriptorEntity.setId(2L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 4L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(configurationId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(6L, 7L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(8L);

        Mockito.when(descriptorTypeRepository.findFirstByType(Mockito.any())).thenReturn(Optional.of(descriptorTypeEntity));
        Mockito.when(registeredDescriptorRepository.findByTypeIdAndFrequency(Mockito.any(), Mockito.any())).thenReturn(List.of(registeredDescriptorEntity));
        setupCreatConfigMocks(descriptorConfigEntity, configContextEntity, fieldValueEntity, definedFieldEntity);

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, descriptorTypeRepository, definedFieldRepository, descriptorConfigRepository, configContextRepository,
            fieldValueRepository,
            encryptionUtility);
        List<ConfigurationModel> configurationModelList = configurationAccessor.getChannelConfigurationsByFrequency(frequencyType);

        assertEquals(1, configurationModelList.size());
        ConfigurationModel configurationModel = configurationModelList.get(0);
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getChannelConfigurationsByFrequencyNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        try {
            configurationAccessor.getChannelConfigurationsByFrequency(null);
            fail("Null frequencyType did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getConfigurationsByDescriptorKeyAndContextTest() throws Exception {
        final Long descriptorId = 3L;
        final Long configurationId = 5L;

        DescriptorKey descriptorKey = createDescriptorKey("descriptorKeyName");
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 4L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(configurationId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        configContextEntity.setId(3L);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name", 1L);
        registeredDescriptorEntity.setId(2L);
        FieldValueEntity fieldValueEntity = new FieldValueEntity(6L, 7L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(8L);

        Mockito.when(descriptorConfigRepository.findByDescriptorIdAndContextId(Mockito.any(), Mockito.any())).thenReturn(List.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findFirstByContext(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(registeredDescriptorRepository.findFirstByName(Mockito.any())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));
        EncryptionUtility encryptionUtility = createEncryptionUtility();

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, null, definedFieldRepository, descriptorConfigRepository, configContextRepository,
            fieldValueRepository,
            encryptionUtility);
        List<ConfigurationModel> configurationModelList = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey, configContextEnum);

        assertEquals(1, configurationModelList.size());
        ConfigurationModel configurationModel = configurationModelList.get(0);
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getConfigurationsByDescriptorKeyAndContextNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        try {
            configurationAccessor.getConfigurationsByDescriptorKeyAndContext(null, null);
            fail("Null descriptorKey and context did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void createConfigurationTest() throws Exception {
        final Long descriptorId = 3L;
        final Long configurationId = 5L;

        DescriptorKey descriptorKey = createDescriptorKey("descriptorKeyName");
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create("channel.common.name");
        configurationFieldModel.setFieldValue(fieldValue);
        List<ConfigurationFieldModel> configuredFields = List.of(configurationFieldModel);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name", 1L);
        registeredDescriptorEntity.setId(descriptorId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        configContextEntity.setId(3L);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(4L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(5L, 6L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(configurationId);

        Mockito.when(registeredDescriptorRepository.findFirstByName(Mockito.any())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(configContextRepository.findFirstByContext(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(definedFieldRepository.findByDescriptorIdAndContext(Mockito.any(), Mockito.any())).thenReturn(List.of(definedFieldEntity));
        Mockito.when(descriptorConfigRepository.save(Mockito.any())).thenReturn(descriptorConfigEntity);
        Mockito.when(definedFieldRepository.findFirstByKey(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, null, definedFieldRepository, descriptorConfigRepository, configContextRepository,
            fieldValueRepository, null);
        ConfigurationModel configurationModel = configurationAccessor.createConfiguration(descriptorKey, configContextEnum, configuredFields);

        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void createConfigurationNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        try {
            configurationAccessor.createConfiguration(null, null, null);
            fail("Null descriptorKey, context, and configuredFields did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getConfigurationByDescriptorNameAndContextNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        try {
            configurationAccessor.getConfigurationsByDescriptorNameAndContext(null, configContextEnum);
            fail("Null descriptorName did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }

        try {
            configurationAccessor.getConfigurationsByDescriptorNameAndContext("descriptorName", null);
            fail("Null context did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void updateConfigurationTest() throws Exception {
        Long configurationId = 2L;
        Long descriptorId = 3L;

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create("channel.common.name");
        configurationFieldModel.setFieldValue(fieldValue);
        List<ConfigurationFieldModel> configuredFields = List.of(configurationFieldModel);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 4L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(configurationId);
        FieldValueEntity fieldValueEntity = new FieldValueEntity(5L, 6L, fieldValue);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(7L);

        Mockito.when(descriptorConfigRepository.findById(Mockito.any())).thenReturn(Optional.of(descriptorConfigEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(definedFieldRepository.findFirstByKey(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, definedFieldRepository, descriptorConfigRepository, configContextRepository,
            fieldValueRepository, null);
        ConfigurationModel configurationModel = configurationAccessor.updateConfiguration(1L, configuredFields);

        Mockito.verify(fieldValueRepository).deleteAll(Mockito.any());
        Mockito.verify(descriptorConfigRepository).save(Mockito.any());

        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void updateConfigurationNullTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        try {
            configurationAccessor.updateConfiguration(null, null);
            fail("Null descriptorConfigId and configuredFields did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void deleteConfigurationTest() throws Exception {
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 2L, "dateCreated", "lastUpdated", configContextEnum);

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, descriptorConfigRepository, null, null, null);
        configurationAccessor.deleteConfiguration(configurationModel);

        Mockito.verify(descriptorConfigRepository).deleteById(Mockito.any());
    }

    @Test
    public void deleteConfigurationNullConfigModelTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        try {
            ConfigurationModel configurationModel = null;
            configurationAccessor.deleteConfiguration(configurationModel);
            fail("Null configurationModel did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void deleteConfigurationNullDescriptorConfigIdTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        try {
            Long descriptorConfigId = null;
            configurationAccessor.deleteConfiguration(descriptorConfigId);
            fail("Null descriptorConfigId did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void decryptTest() throws Exception {
        final String decryptedString = "decryptedString";
        final String providerConfigName = "provider-config-name-test";
        final String emptyProviderConfigName = "bad-config-name";
        final Long fieldId = 1L;
        final Long descriptorId = 4L;
        final Long configurationId = 6L;

        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, true);
        definedFieldEntity.setId(fieldId);
        FieldValueEntity fieldValueEntity = new FieldValueEntity(2L, 3L, fieldValue);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(descriptorId, 5L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(configurationId);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());

        EncryptionUtility encryptionUtilityDecrypt = Mockito.mock(EncryptionUtility.class);

        Mockito.when(definedFieldRepository.findFirstByKey(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));
        Mockito.when(fieldValueRepository.findAllByFieldIdAndValue(fieldId, providerConfigName)).thenReturn(List.of(fieldValueEntity));
        Mockito.when(fieldValueRepository.findAllByFieldIdAndValue(fieldId, emptyProviderConfigName)).thenReturn(List.of());
        setupGetJobMocks(descriptorConfigEntity, configContextEntity, fieldValueEntity, definedFieldEntity);
        Mockito.when(encryptionUtilityDecrypt.decrypt(Mockito.any())).thenReturn(decryptedString);

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, definedFieldRepository, descriptorConfigRepository, configContextRepository, fieldValueRepository, encryptionUtilityDecrypt);
        Optional<ConfigurationModel> configurationModelOptional = configurationAccessor.getProviderConfigurationByName(providerConfigName);

        assertTrue(configurationModelOptional.isPresent());

        ConfigurationModel configurationModel = configurationModelOptional.get();
        testConfigurationModel(configurationId, descriptorId, configurationModel);
        assertEquals(decryptedString, configurationModel.getField(fieldKey).get().getFieldValue().get());
    }

    private void setupGetJobMocks(DescriptorConfigEntity descriptorConfigEntity, ConfigContextEntity configContextEntity, FieldValueEntity fieldValueEntity, DefinedFieldEntity definedFieldEntity) {
        Mockito.when(descriptorConfigRepository.findById(Mockito.any())).thenReturn(Optional.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));
    }

    private void setupCreatConfigMocks(DescriptorConfigEntity descriptorConfigEntity, ConfigContextEntity configContextEntity, FieldValueEntity fieldValueEntity, DefinedFieldEntity definedFieldEntity) {
        Mockito.when(descriptorConfigRepository.findByDescriptorId(Mockito.any())).thenReturn(List.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));
    }

    private void testConfigurationModel(Long configurationId, Long descriptorId, ConfigurationModel configurationModel) {
        assertEquals(configurationId, configurationModel.getConfigurationId());
        assertEquals(descriptorId, configurationModel.getDescriptorId());
        assertEquals(configContextEnum, configurationModel.getDescriptorContext());
    }

    private EncryptionUtility createEncryptionUtility() {
        alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertEncryptionPassword()).thenReturn(Optional.of(TEST_PASSWORD));
        Mockito.when(alertProperties.getAlertEncryptionGlobalSalt()).thenReturn(Optional.of(TEST_SALT));
        Mockito.when(alertProperties.getAlertConfigHome()).thenReturn(TEST_DIRECTORY);
        Mockito.when(alertProperties.getAlertSecretsDir()).thenReturn(TEST_SECRETS_DIRECTORY);
        filePersistenceUtil = new FilePersistenceUtil(alertProperties, new Gson());
        return new EncryptionUtility(alertProperties, filePersistenceUtil);
    }

    private DescriptorKey createDescriptorKey(String key) {
        DescriptorKey testDescriptorKey = new DescriptorKey(key, key) {};
        return testDescriptorKey;
    }

}
