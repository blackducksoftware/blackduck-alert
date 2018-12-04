package com.synopsys.integration.alert.database.api.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.descriptor.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.entity.descriptor.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.descriptor.DescriptorFieldRepository;
import com.synopsys.integration.alert.database.repository.descriptor.FieldValueRepository;
import com.synopsys.integration.alert.database.repository.descriptor.RegisteredDescriptorRepository;

public class ConfigurationAccessorTestIT extends AlertIntegrationTest {
    public static final String DESCRIPTOR_NAME = "Test Descriptor";
    public static final String DESCRIPTOR_TYPE = "Test Component";
    public static final String FIELD_KEY_INSENSITIVE = "testInsensitiveField";
    public static final String FIELD_KEY_SENSITIVE = "testSensitiveField";
    public static final DescriptorFieldModel DESCRIPTOR_FIELD_INSENSITIVE = new DescriptorFieldModel(FIELD_KEY_INSENSITIVE, Boolean.FALSE);
    public static final DescriptorFieldModel DESCRIPTOR_FIELD_SENSITIVE = new DescriptorFieldModel(FIELD_KEY_SENSITIVE, Boolean.TRUE);

    @Autowired
    private DescriptorAccessor descriptorAccessor;
    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private DescriptorFieldRepository descriptorFieldRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigsRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;
    @Autowired
    private EncryptionUtility encryptionUtility;

    private ConfigurationAccessor configurationAccessor;

    @Before
    public void init() throws AlertDatabaseConstraintException {
        configurationAccessor = new ConfigurationAccessor(registeredDescriptorRepository, descriptorFieldRepository, descriptorConfigsRepository, fieldValueRepository, encryptionUtility);
        descriptorAccessor.registerDescriptor(DESCRIPTOR_NAME, DESCRIPTOR_TYPE, Arrays.asList(DESCRIPTOR_FIELD_INSENSITIVE, DESCRIPTOR_FIELD_SENSITIVE));
    }

    @After
    public void cleanup() throws AlertDatabaseConstraintException {
        descriptorAccessor.unregisterDescriptor(DESCRIPTOR_NAME);

        registeredDescriptorRepository.deleteAll();
        descriptorFieldRepository.deleteAll();
        descriptorConfigsRepository.deleteAll();
        fieldValueRepository.deleteAll();
    }

    @Test
    public void createConfigTest() throws AlertDatabaseConstraintException {
        final ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        final ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);

        final ConfigurationModel createdConfig = configurationAccessor.createConfiguration(DESCRIPTOR_NAME, Arrays.asList(configField1, configField2));
        assertTrue(createdConfig.getCopyOfFieldList().contains(configField1));
        assertTrue(createdConfig.getCopyOfFieldList().contains(configField2));

        final Optional<DescriptorConfigEntity> configEntityOptional = descriptorConfigsRepository.findById(createdConfig.getConfigurationId());
        assertTrue(configEntityOptional.isPresent());
    }

    @Test
    public void createEmptyConfigTest() throws AlertDatabaseConstraintException {
        final ConfigurationModel createdConfig = configurationAccessor.createEmptyConfiguration(DESCRIPTOR_NAME);
        assertTrue(createdConfig.getCopyOfFieldList().isEmpty());

        final Optional<DescriptorConfigEntity> configEntityOptional = descriptorConfigsRepository.findById(createdConfig.getConfigurationId());
        assertTrue(configEntityOptional.isPresent());
    }

    @Test
    public void createConfigWithEmptyDescriptorNameTest() {
        createConfigWithEmptyDescriptorNameTestHelper(null);
        createConfigWithEmptyDescriptorNameTestHelper("");
    }

    @Test
    public void createConfigWithInvalidDescriptorNameTest() {
        try {
            configurationAccessor.createEmptyConfiguration("invalid descriptor name");
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("No descriptor with the provided name was registered", e.getMessage());
        }
    }

    @Test
    public void getConfigsByNameTest() throws AlertDatabaseConstraintException {
        final ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        final ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        configurationAccessor.createConfiguration(DESCRIPTOR_NAME, Arrays.asList(configField1));
        configurationAccessor.createConfiguration(DESCRIPTOR_NAME, Arrays.asList(configField2));

        final List<ConfigurationModel> configurationsForDescriptor = configurationAccessor.getConfigurationsByName(DESCRIPTOR_NAME);
        assertEquals(2, configurationsForDescriptor.size());
    }

    @Test
    public void getConfigurationByIdTest() throws AlertDatabaseConstraintException {
        final ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        final ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        final ConfigurationModel configurationModel1 = configurationAccessor.createConfiguration(DESCRIPTOR_NAME, Arrays.asList(configField1));
        final ConfigurationModel configurationModel2 = configurationAccessor.createConfiguration(DESCRIPTOR_NAME, Arrays.asList(configField2));

        final Optional<ConfigurationModel> optionalFoundConfig1 = configurationAccessor.getConfigurationById(configurationModel1.getConfigurationId());
        assertTrue(optionalFoundConfig1.isPresent());
        final ConfigurationModel foundConfig1 = optionalFoundConfig1.get();
        assertEquals(configurationModel1.getDescriptorId(), foundConfig1.getDescriptorId());
        assertEquals(configurationModel1.getConfigurationId(), foundConfig1.getConfigurationId());

        final Optional<ConfigurationModel> optionalFoundConfig2 = configurationAccessor.getConfigurationById(configurationModel2.getConfigurationId());
        assertTrue(optionalFoundConfig2.isPresent());
        final ConfigurationModel foundConfig2 = optionalFoundConfig2.get();
        assertEquals(configurationModel2.getDescriptorId(), foundConfig2.getDescriptorId());
        assertEquals(configurationModel2.getConfigurationId(), foundConfig2.getConfigurationId());
    }

    @Test
    public void getConfigurationsByTypeTest() throws AlertDatabaseConstraintException {
        final String descriptorName2 = DESCRIPTOR_NAME + "2";
        final String descriptorType2 = DESCRIPTOR_TYPE + "2";
        final DescriptorFieldModel descriptorFieldModel = new DescriptorFieldModel(FIELD_KEY_SENSITIVE, Boolean.TRUE);
        descriptorAccessor.registerDescriptor(descriptorName2, descriptorType2, Collections.singletonList(descriptorFieldModel));

        final String value1 = "value 1";
        final String value2 = "value 2";
        final ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        configField1.setFieldValue(value1);
        final ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        configField2.setFieldValue(value2);
        final ConfigurationModel savedConfig1 = configurationAccessor.createConfiguration(DESCRIPTOR_NAME, Arrays.asList(configField1));
        final ConfigurationModel savedConfig2 = configurationAccessor.createConfiguration(descriptorName2, Arrays.asList(configField2));

        final List<ConfigurationModel> configurationsForDescriptor1 = configurationAccessor.getConfigurationsByType(DESCRIPTOR_TYPE);
        assertEquals(1, configurationsForDescriptor1.size());
        final ConfigurationModel foundModel1 = configurationsForDescriptor1.get(0);
        assertEquals(savedConfig1.getDescriptorId(), foundModel1.getDescriptorId());
        assertEquals(savedConfig1.getConfigurationId(), foundModel1.getConfigurationId());

        final List<ConfigurationModel> configurationsForDescriptor2 = configurationAccessor.getConfigurationsByType(descriptorType2);
        assertEquals(1, configurationsForDescriptor2.size());
        final ConfigurationModel foundModel2 = configurationsForDescriptor2.get(0);
        assertEquals(savedConfig2.getDescriptorId(), foundModel2.getDescriptorId());
        assertEquals(savedConfig2.getConfigurationId(), foundModel2.getConfigurationId());

        // Test that the values persist
        final Optional<ConfigurationFieldModel> optionalSavedField1 = savedConfig1.getField(configField1.getFieldKey());
        assertTrue(optionalSavedField1.isPresent());
        final ConfigurationFieldModel savedField1 = optionalSavedField1.get();
        assertEquals(value1, savedField1.getFieldValue().orElse(null));

        final Optional<ConfigurationFieldModel> optionalSavedField2 = savedConfig2.getField(configField2.getFieldKey());
        assertTrue(optionalSavedField2.isPresent());
        final ConfigurationFieldModel savedField2 = optionalSavedField2.get();
        assertEquals(value2, savedField2.getFieldValue().orElse(null));
    }

    @Test
    public void getConfigurationsWithNullIdTest() {
        try {
            configurationAccessor.getConfigurationById(null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The config id cannot be null", e.getMessage());
        }
    }

    @Test
    public void getConfigurationsWithEmptyDescriptorNameTest() {
        getConfigByNameWithEmptyDescriptorNameTestHelper(null);
        getConfigByNameWithEmptyDescriptorNameTestHelper("");

        getConfigByTypeWithEmptyDescriptorNameTestHelper(null);
        getConfigByTypeWithEmptyDescriptorNameTestHelper("");
    }

    @Test
    public void updateConfigurationTest() throws AlertDatabaseConstraintException {
        final String initialValue = "initial value";
        final ConfigurationFieldModel originalField = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        originalField.setFieldValue(initialValue);

        final ConfigurationModel createdModel = configurationAccessor.createConfiguration(DESCRIPTOR_NAME, Arrays.asList(originalField));
        final List<ConfigurationFieldModel> copyOfFieldList = createdModel.getCopyOfFieldList();
        assertEquals(1, copyOfFieldList.size());
        final Optional<String> optionalValue = copyOfFieldList.get(0).getFieldValue();
        assertTrue(optionalValue.isPresent());
        assertEquals(initialValue, optionalValue.get());

        final String additionalValue = "additional value";
        final ConfigurationFieldModel newFieldWithSameKey = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        newFieldWithSameKey.setFieldValue(additionalValue);

        final ConfigurationModel updatedModel = configurationAccessor.updateConfiguration(createdModel.getConfigurationId(), Arrays.asList(originalField, newFieldWithSameKey));
        final List<ConfigurationFieldModel> configuredFields = updatedModel.getCopyOfFieldList();
        assertEquals(1, configuredFields.size());
        final ConfigurationFieldModel configuredField = configuredFields.get(0);
        assertEquals(originalField, configuredField);
        assertTrue(configuredField.getFieldValues().contains(initialValue));
        assertTrue(configuredField.getFieldValues().contains(additionalValue));
    }

    @Test
    public void updateConfigurationWithNullIdTest() {
        try {
            configurationAccessor.updateConfiguration(null, null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The config id cannot be null", e.getMessage());
        }
    }

    @Test
    public void updateConfigurationWithInvalidIdTest() {
        final Long invalidId = Long.MAX_VALUE;
        try {
            configurationAccessor.updateConfiguration(invalidId, null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("A config with that id did not exist", e.getMessage());
        }
    }

    @Test
    public void updateConfigurationWithInvalidFieldKeyTest() {
        try {
            final ConfigurationModel configurationModel = configurationAccessor.createEmptyConfiguration(DESCRIPTOR_NAME);
            final ConfigurationFieldModel field = ConfigurationFieldModel.create(null);
            configurationAccessor.updateConfiguration(configurationModel.getConfigurationId(), Arrays.asList(field));
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Field key cannot be empty", e.getMessage());
        }
    }

    @Test
    public void deleteConfigurationTest() throws AlertDatabaseConstraintException {
        final ConfigurationModel createdModel1 = configurationAccessor.createEmptyConfiguration(DESCRIPTOR_NAME);
        final ConfigurationModel createdModel2 = configurationAccessor.createEmptyConfiguration(DESCRIPTOR_NAME);
        assertEquals(2, descriptorConfigsRepository.findAll().size());

        configurationAccessor.deleteConfiguration(createdModel1);
        assertEquals(1, descriptorConfigsRepository.findAll().size());

        configurationAccessor.deleteConfiguration(createdModel2);
        assertEquals(0, descriptorConfigsRepository.findAll().size());
    }

    @Test
    public void deleteConfigurationWithInvalidArgsTest() {
        try {
            configurationAccessor.deleteConfiguration((ConfigurationModel) null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Cannot delete a null object from the database", e.getMessage());
        }
        try {
            configurationAccessor.deleteConfiguration((Long) null);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("The config id cannot be null", e.getMessage());
        }
    }

    @Test
    public void configurationModelTest() throws AlertDatabaseConstraintException {
        final ConfigurationModel configurationModel = configurationAccessor.createEmptyConfiguration(DESCRIPTOR_NAME);
        assertNotNull(configurationModel.getConfigurationId());
        assertNotNull(configurationModel.getDescriptorId());
        assertNotNull(configurationModel.getCopyOfFieldList());
        assertNotNull(configurationModel.getCopyOfKeyToFieldMap());
    }

    private void createConfigWithEmptyDescriptorNameTestHelper(final String descriptorName) {
        try {
            configurationAccessor.createEmptyConfiguration(descriptorName);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Descriptor name cannot be empty", e.getMessage());
        }
    }

    private void getConfigByNameWithEmptyDescriptorNameTestHelper(final String descriptorName) {
        try {
            configurationAccessor.getConfigurationsByName(descriptorName);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Descriptor name cannot be empty", e.getMessage());
        }
    }

    private void getConfigByTypeWithEmptyDescriptorNameTestHelper(final String descriptorType) {
        try {
            configurationAccessor.getConfigurationsByType(descriptorType);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertEquals("Descriptor type cannot be empty", e.getMessage());
        }
    }
}
