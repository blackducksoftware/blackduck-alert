package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
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
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class DefaultConfigurationModelConfigurationAccessorTest {
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
    public void getProviderConfigurationByNameTest() {
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

        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = new DefaultConfigurationModelConfigurationAccessor(null, definedFieldRepository, descriptorConfigRepository, configContextRepository, fieldValueRepository, encryptionUtility);
        Optional<ConfigurationModel> configurationModelOptional = configurationModelConfigurationAccessor.getProviderConfigurationByName(providerConfigName);
        Optional<ConfigurationModel> configurationModelProviderConfigsEmpty = configurationModelConfigurationAccessor.getProviderConfigurationByName(emptyProviderConfigName);

        assertTrue(configurationModelOptional.isPresent());
        assertFalse(configurationModelProviderConfigsEmpty.isPresent());

        ConfigurationModel configurationModel = configurationModelOptional.get();
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getConfigurationByIdEmptyTest() {
        Mockito.when(descriptorConfigRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = new DefaultConfigurationModelConfigurationAccessor(null, null, descriptorConfigRepository, null, null, null);
        Optional<ConfigurationModel> configurationModelOptional = configurationModelConfigurationAccessor.getConfigurationById(1L);

        assertFalse(configurationModelOptional.isPresent());
    }

    @Test
    public void getConfigurationsByDescriptorKeyTest() {
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

        Mockito.when(descriptorConfigRepository.findByDescriptorName(descriptorKey.getUniversalKey())).thenReturn(List.of(descriptorConfigEntity));
        Mockito.when(registeredDescriptorRepository.findFirstByName(badDescriptorKey.getUniversalKey())).thenReturn(Optional.empty());
        setupCreatConfigMocks(descriptorConfigEntity, configContextEntity, fieldValueEntity, definedFieldEntity);

        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = new DefaultConfigurationModelConfigurationAccessor(
            registeredDescriptorRepository,
            definedFieldRepository,
            descriptorConfigRepository,
            configContextRepository,
            fieldValueRepository,
            encryptionUtility
        );
        List<ConfigurationModel> configurationModelList = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
        List<ConfigurationModel> configurationModelListEmpty = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(badDescriptorKey);

        assertEquals(1, configurationModelList.size());
        assertTrue(configurationModelListEmpty.isEmpty());
        ConfigurationModel configurationModel = configurationModelList.get(0);
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getConfigurationsByDescriptorTypeTest() {
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

        Mockito.when(descriptorConfigRepository.findByDescriptorType(Mockito.eq(descriptorType.name()))).thenReturn(List.of(descriptorConfigEntity));
        setupCreatConfigMocks(descriptorConfigEntity, configContextEntity, fieldValueEntity, definedFieldEntity);

        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = new DefaultConfigurationModelConfigurationAccessor(
            registeredDescriptorRepository,
            definedFieldRepository,
            descriptorConfigRepository,
            configContextRepository,
            fieldValueRepository,
            encryptionUtility
        );
        List<ConfigurationModel> configurationModelList = configurationModelConfigurationAccessor.getConfigurationsByDescriptorType(descriptorType);

        assertEquals(1, configurationModelList.size());
        ConfigurationModel configurationModel = configurationModelList.get(0);
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void getConfigurationsByDescriptorKeyAndContextTest() {
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

        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = new DefaultConfigurationModelConfigurationAccessor(
            registeredDescriptorRepository, definedFieldRepository, descriptorConfigRepository, configContextRepository, fieldValueRepository, encryptionUtility);
        List<ConfigurationModel> configurationModelList = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey, configContextEnum);

        assertEquals(1, configurationModelList.size());
        ConfigurationModel configurationModel = configurationModelList.get(0);
        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void createConfigurationTest() {
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

        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = new DefaultConfigurationModelConfigurationAccessor(registeredDescriptorRepository, definedFieldRepository, descriptorConfigRepository, configContextRepository,
            fieldValueRepository, null);
        ConfigurationModel configurationModel = configurationModelConfigurationAccessor.createConfiguration(descriptorKey, configContextEnum, configuredFields);

        testConfigurationModel(configurationId, descriptorId, configurationModel);
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

        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = new DefaultConfigurationModelConfigurationAccessor(null, definedFieldRepository, descriptorConfigRepository, configContextRepository,
            fieldValueRepository, null);
        ConfigurationModel configurationModel = configurationModelConfigurationAccessor.updateConfiguration(1L, configuredFields);

        Mockito.verify(fieldValueRepository).deleteAll(Mockito.any());
        Mockito.verify(descriptorConfigRepository).save(Mockito.any());

        testConfigurationModel(configurationId, descriptorId, configurationModel);
    }

    @Test
    public void deleteConfigurationTest() {
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 2L, "dateCreated", "lastUpdated", configContextEnum);

        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = new DefaultConfigurationModelConfigurationAccessor(null, null, descriptorConfigRepository, null, null, null);
        configurationModelConfigurationAccessor.deleteConfiguration(configurationModel);

        Mockito.verify(descriptorConfigRepository).deleteById(Mockito.any());
    }

    @Test
    public void decryptTest() {
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

        DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = new DefaultConfigurationModelConfigurationAccessor(null, definedFieldRepository, descriptorConfigRepository, configContextRepository, fieldValueRepository, encryptionUtilityDecrypt);
        Optional<ConfigurationModel> configurationModelOptional = configurationModelConfigurationAccessor.getProviderConfigurationByName(providerConfigName);

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
