package com.synopsys.integration.alert.database.api.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.DefaultConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.DescriptorMocker;

@Transactional
@AlertIntegrationTest
public class ConfigurationModelConfigurationAccessorTestIT {
    public static final String DESCRIPTOR_NAME = "Test Descriptor";
    public static final String FIELD_KEY_INSENSITIVE = "testInsensitiveField";
    public static final String FIELD_KEY_SENSITIVE = "testSensitiveField";

    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DefinedFieldRepository definedFieldRepository;
    @Autowired
    private ConfigContextRepository configContextRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigsRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;
    @Autowired
    private EncryptionUtility encryptionUtility;
    @Autowired
    private DescriptorMocker descriptorMocker;

    private DefaultConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @BeforeEach
    public void init() {
        descriptorConfigsRepository.flush();
        descriptorConfigsRepository.deleteAllInBatch();
        configurationModelConfigurationAccessor = new DefaultConfigurationModelConfigurationAccessor(
            registeredDescriptorRepository, definedFieldRepository, descriptorConfigsRepository, configContextRepository, fieldValueRepository, encryptionUtility);
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME, DescriptorType.PROVIDER);
        descriptorMocker.addFieldToDescriptor(DESCRIPTOR_NAME, FIELD_KEY_INSENSITIVE, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION), Boolean.FALSE);
        descriptorMocker.addFieldToDescriptor(DESCRIPTOR_NAME, FIELD_KEY_SENSITIVE, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION), Boolean.TRUE);
    }

    @AfterEach
    public void cleanup() {
        descriptorMocker.cleanUpDescriptors();
    }

    private DescriptorKey createDescriptorKey(String key) {
        DescriptorKey testDescriptorKey = new DescriptorKey(key, key) {};
        return testDescriptorKey;
    }

    @Test
    public void createConfigTest() {
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel createdConfig = configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of(configField1, configField2));
        assertTrue(createdConfig.getCopyOfFieldList().contains(configField1));
        assertTrue(createdConfig.getCopyOfFieldList().contains(configField2));

        Optional<DescriptorConfigEntity> configEntityOptional = descriptorConfigsRepository.findById(createdConfig.getConfigurationId());
        assertTrue(configEntityOptional.isPresent());
    }

    @Test
    public void getConfigsByNameTest() {
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField1));
        configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField2));

        List<ConfigurationModel> configurationsForDescriptor = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
        assertEquals(2, configurationsForDescriptor.size());
    }

    @Test
    public void getConfigurationByIdTest() {
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel configurationModel1 = configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField1));
        ConfigurationModel configurationModel2 = configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField2));

        Optional<ConfigurationModel> optionalFoundConfig1 = configurationModelConfigurationAccessor.getConfigurationById(configurationModel1.getConfigurationId());
        assertTrue(optionalFoundConfig1.isPresent());
        ConfigurationModel foundConfig1 = optionalFoundConfig1.get();
        assertEquals(configurationModel1.getDescriptorId(), foundConfig1.getDescriptorId());
        assertEquals(configurationModel1.getConfigurationId(), foundConfig1.getConfigurationId());

        Optional<ConfigurationModel> optionalFoundConfig2 = configurationModelConfigurationAccessor.getConfigurationById(configurationModel2.getConfigurationId());
        assertTrue(optionalFoundConfig2.isPresent());
        ConfigurationModel foundConfig2 = optionalFoundConfig2.get();
        assertEquals(configurationModel2.getDescriptorId(), foundConfig2.getDescriptorId());
        assertEquals(configurationModel2.getConfigurationId(), foundConfig2.getConfigurationId());
    }

    @Test
    public void getConfigurationsByDescriptorTypeTest() {
        List<ConfigurationModel> configurationModels = configurationModelConfigurationAccessor.getConfigurationsByDescriptorType(DescriptorType.CHANNEL);
        assertTrue(configurationModels.isEmpty());
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField1));
        configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField2));
        configurationModels = configurationModelConfigurationAccessor.getConfigurationsByDescriptorType(DescriptorType.PROVIDER);
        assertFalse(configurationModels.isEmpty());
    }

    @Test
    public void updateConfigurationMultipleValueTest() throws AlertConfigurationException {
        final String initialValue = "initial value";
        ConfigurationFieldModel originalField = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        originalField.setFieldValue(initialValue);
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel createdModel = configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(originalField));
        List<ConfigurationFieldModel> copyOfFieldList = createdModel.getCopyOfFieldList();
        assertEquals(1, copyOfFieldList.size());
        Optional<String> optionalValue = copyOfFieldList.get(0).getFieldValue();
        assertTrue(optionalValue.isPresent());
        assertEquals(initialValue, optionalValue.get());

        final String additionalValue = "additional value";
        ConfigurationFieldModel newFieldWithSameKey = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        newFieldWithSameKey.setFieldValue(additionalValue);

        ConfigurationModel updatedModel = configurationModelConfigurationAccessor.updateConfiguration(createdModel.getConfigurationId(), Arrays.asList(originalField, newFieldWithSameKey));
        List<ConfigurationFieldModel> configuredFields = updatedModel.getCopyOfFieldList();
        assertEquals(1, configuredFields.size());
        ConfigurationFieldModel configuredField = configuredFields.get(0);
        assertEquals(originalField, configuredField);
        assertTrue(configuredField.getFieldValues().contains(initialValue));
        assertTrue(configuredField.getFieldValues().contains(additionalValue));
        List<FieldValueEntity> databaseFieldValues = fieldValueRepository.findByConfigId(updatedModel.getConfigurationId());
        assertNotNull(databaseFieldValues);
        assertEquals(2, databaseFieldValues.size());
        assertEquals(configuredField.getFieldValues().size(), databaseFieldValues.size());
    }

    @Test
    public void updateConfigurationReplaceValueTest() throws AlertConfigurationException {
        final String initialValue = "initial value";
        ConfigurationFieldModel originalField = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        originalField.setFieldValue(initialValue);

        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel createdModel = configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(originalField));
        List<ConfigurationFieldModel> copyOfFieldList = createdModel.getCopyOfFieldList();
        assertEquals(1, copyOfFieldList.size());
        Optional<String> optionalValue = copyOfFieldList.get(0).getFieldValue();
        assertTrue(optionalValue.isPresent());
        assertEquals(initialValue, optionalValue.get());

        final String additionalValue = "additional value";
        ConfigurationFieldModel newFieldWithSameKey = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        newFieldWithSameKey.setFieldValue(additionalValue);

        ConfigurationModel updatedModel = configurationModelConfigurationAccessor.updateConfiguration(createdModel.getConfigurationId(), Arrays.asList(newFieldWithSameKey));
        List<ConfigurationFieldModel> configuredFields = updatedModel.getCopyOfFieldList();
        assertEquals(1, configuredFields.size());
        ConfigurationFieldModel configuredField = configuredFields.get(0);
        assertEquals(originalField, configuredField);
        assertTrue(configuredField.getFieldValues().contains(additionalValue));
        List<FieldValueEntity> databaseFieldValues = fieldValueRepository.findByConfigId(updatedModel.getConfigurationId());
        assertNotNull(databaseFieldValues);
        assertEquals(1, databaseFieldValues.size());
    }

    @Test
    public void updateConfigurationWithInvalidIdTest() {
        final Long invalidId = Long.MAX_VALUE;
        try {
            configurationModelConfigurationAccessor.updateConfiguration(invalidId, null);
            fail("Expected exception to be thrown");
        } catch (AlertConfigurationException e) {
            // Success
        }
    }

    @Test
    public void deleteConfigurationTest() {
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel createdModel1 = configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of());
        ConfigurationModel createdModel2 = configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of());
        List<ConfigurationModel> foundModels = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
        assertEquals(2, foundModels.size());

        configurationModelConfigurationAccessor.deleteConfiguration(createdModel1);
        List<ConfigurationModel> afterFirstDeletion = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
        assertEquals(foundModels.size() - 1, afterFirstDeletion.size());

        configurationModelConfigurationAccessor.deleteConfiguration(createdModel2);
        List<ConfigurationModel> afterSecondDeletion = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
        assertEquals(foundModels.size() - 2, afterSecondDeletion.size());
    }

    @Test
    public void configurationModelTest() {
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel configurationModel = configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of());
        assertNotNull(configurationModel.getConfigurationId());
        assertNotNull(configurationModel.getDescriptorId());
        assertNotNull(configurationModel.getCopyOfFieldList());
        assertNotNull(configurationModel.getCopyOfKeyToFieldMap());
    }

}
