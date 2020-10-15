package com.synopsys.integration.alert.database.api.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.DefaultConfigurationAccessor;
import com.synopsys.integration.alert.database.api.DefaultJobAccessor;
import com.synopsys.integration.alert.database.configuration.ConfigGroupEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.ConfigGroupRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.DescriptorMocker;

@Transactional
public class ConfigurationAccessorTestIT extends AlertIntegrationTest {
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
    private ConfigGroupRepository configGroupRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;
    @Autowired
    private EncryptionUtility encryptionUtility;
    @Autowired
    private DescriptorTypeRepository descriptorTypeRepository;
    @Autowired
    private DescriptorMocker descriptorMocker;

    private DefaultJobAccessor jobAccessor;

    private DefaultConfigurationAccessor configurationAccessor;

    @BeforeEach
    public void init() {
        descriptorConfigsRepository.flush();
        descriptorConfigsRepository.deleteAllInBatch();
        configurationAccessor = new DefaultConfigurationAccessor(
            registeredDescriptorRepository, descriptorTypeRepository, definedFieldRepository, descriptorConfigsRepository, configContextRepository, fieldValueRepository, encryptionUtility);
        jobAccessor = new DefaultJobAccessor(configGroupRepository, configurationAccessor);
        descriptorMocker.registerDescriptor(DESCRIPTOR_NAME, DescriptorType.PROVIDER);
        descriptorMocker.addFieldToDescriptor(DESCRIPTOR_NAME, FIELD_KEY_INSENSITIVE, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION), Boolean.FALSE);
        descriptorMocker.addFieldToDescriptor(DESCRIPTOR_NAME, FIELD_KEY_SENSITIVE, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION), Boolean.TRUE);
    }

    @AfterEach
    public void cleanup() {
        descriptorMocker.cleanUpDescriptors();
    }

    private DescriptorKey createDescriptorKey(String key) {
        DescriptorKey testDescriptorKey = new DescriptorKey() {
            private static final long serialVersionUID = 42094944704702165L;

            @Override
            public String getUniversalKey() {
                return key;
            }

            @Override
            public String getDisplayName() {
                return key;
            }
        };
        return testDescriptorKey;
    }

    @Test
    public void getAllJobsTest() throws AlertDatabaseConstraintException {
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel configurationModel1 = configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of(configField1));
        ConfigurationModel configurationModel2 = configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of(configField2));

        UUID jobId = UUID.randomUUID();
        ConfigGroupEntity entityToSave1 = new ConfigGroupEntity(configurationModel1.getConfigurationId(), jobId);
        configGroupRepository.save(entityToSave1);
        ConfigGroupEntity entityToSave2 = new ConfigGroupEntity(configurationModel2.getConfigurationId(), jobId);
        configGroupRepository.save(entityToSave2);
        assertEquals(2, configGroupRepository.findAll().size());

        List<ConfigurationJobModel> allJobs = jobAccessor.getAllJobs();
        assertEquals(1, allJobs.size());
        assertEquals(jobId, allJobs.get(0).getJobId());
    }

    @Test
    public void getJobByIdTest() throws AlertDatabaseConstraintException {
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);

        DescriptorKey testDescriptorKey = new DescriptorKey() {
            private static final long serialVersionUID = -6433896789808382044L;

            @Override
            public String getUniversalKey() {
                return DESCRIPTOR_NAME;
            }

            @Override
            public String getDisplayName() {
                return DESCRIPTOR_NAME;
            }
        };

        ConfigurationModel configurationModel1 = configurationAccessor.createConfiguration(testDescriptorKey, ConfigContextEnum.DISTRIBUTION, List.of(configField1));
        ConfigurationModel configurationModel2 = configurationAccessor.createConfiguration(testDescriptorKey, ConfigContextEnum.DISTRIBUTION, List.of(configField2));

        UUID jobId = UUID.randomUUID();
        ConfigGroupEntity entityToSave1 = new ConfigGroupEntity(configurationModel1.getConfigurationId(), jobId);
        configGroupRepository.save(entityToSave1);
        ConfigGroupEntity entityToSave2 = new ConfigGroupEntity(configurationModel2.getConfigurationId(), UUID.randomUUID());
        configGroupRepository.save(entityToSave2);
        assertEquals(2, configGroupRepository.findAll().size());

        ConfigurationJobModel foundJob = jobAccessor.getJobById(jobId).orElseThrow();
        assertEquals(jobId, foundJob.getJobId());
        ConfigurationModel firstModel = foundJob.getCopyOfConfigurations().stream().findFirst().orElseThrow();
        assertEquals(firstModel.getConfigurationId(), configurationModel1.getConfigurationId());
        // TODO add this when it is clear that we should return fields with no values: assertEquals(configurationModel1.getCopyOfFieldList().size(), firstModel.getCopyOfFieldList().size());
    }

    @Test
    public void createJobWithNoFieldsTest() throws AlertDatabaseConstraintException {
        ConfigurationJobModel job = jobAccessor.createJob(Set.of(DESCRIPTOR_NAME), Set.of());
        assertEquals(1, configGroupRepository.findByJobId(job.getJobId()).size());
    }

    @Test
    public void createJobWithFieldsTest() throws AlertDatabaseConstraintException {
        final String fieldValue = "example value";
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        configField1.setFieldValue(fieldValue);

        ConfigurationJobModel job = jobAccessor.createJob(Set.of(DESCRIPTOR_NAME), Set.of(configField1));
        assertEquals(1, configGroupRepository.findByJobId(job.getJobId()).size());

        ConfigurationModel foundConfig = job.getCopyOfConfigurations().stream().findFirst().orElseThrow();
        ConfigurationFieldModel foundField = foundConfig.getCopyOfFieldList().stream().findFirst().orElseThrow();
        assertEquals(configField1.getFieldKey(), foundField.getFieldKey());
        assertEquals(configField1.getFieldValue().orElseThrow(), foundField.getFieldValue().orElseThrow());
    }

    @Test
    public void createJobWithEmptyDescriptorNamesTest() {
        createJobWithEmptyDescriptorNamesTestHelper(null);
        createJobWithEmptyDescriptorNamesTestHelper(Set.of());
    }

    @Test
    public void getJobByIdWithNullTest() {
        try {
            jobAccessor.getJobById(null);
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("The job id cannot be null", e.getMessage());
        }
    }

    @Test
    public void updateJobTest() throws AlertDatabaseConstraintException {
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        configField1.setFieldValue("example");
        ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        configField2.setFieldValue("other example");
        Set<String> descriptorNames = Set.of(DESCRIPTOR_NAME);
        ConfigurationJobModel job = jobAccessor.createJob(descriptorNames, Set.of(configField1, configField2));

        final String newValue = "newValue";
        configField1.setFieldValue(newValue);
        ConfigurationJobModel updatedJob = jobAccessor.updateJob(job.getJobId(), descriptorNames, Set.of(configField1, configField2));
        assertEquals(job.getJobId(), updatedJob.getJobId());

        FieldUtility originalFieldMap = job.getFieldUtility();
        FieldUtility newFieldMap = updatedJob.getFieldUtility();
        assertEquals(newValue, newFieldMap.getString(configField1.getFieldKey()).orElseThrow());
        assertEquals(originalFieldMap.getString(configField2.getFieldKey()), newFieldMap.getString(configField2.getFieldKey()));
    }

    @Test
    public void updateJobWithNullIdTest() {
        try {
            jobAccessor.updateJob(null, Set.of(), Set.of());
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("The job id cannot be null", e.getMessage());
        }
    }

    @Test
    public void deleteJobTest() throws AlertDatabaseConstraintException {
        ConfigurationJobModel job = jobAccessor.createJob(Set.of(DESCRIPTOR_NAME), Set.of());
        assertTrue(!configGroupRepository.findByJobId(job.getJobId()).isEmpty());

        jobAccessor.deleteJob(job.getJobId());
        configGroupRepository.flush();
        List<ConfigGroupEntity> remainingEntries = configGroupRepository.findByJobId(job.getJobId());
        assertEquals(0, remainingEntries.size());
    }

    @Test
    public void deleteJobWithNullIdTest() {
        try {
            jobAccessor.deleteJob(null);
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("The job id cannot be null", e.getMessage());
        }
    }

    @Test
    public void createConfigTest() throws AlertDatabaseConstraintException {
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel createdConfig = configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of(configField1, configField2));
        assertTrue(createdConfig.getCopyOfFieldList().contains(configField1));
        assertTrue(createdConfig.getCopyOfFieldList().contains(configField2));

        Optional<DescriptorConfigEntity> configEntityOptional = descriptorConfigsRepository.findById(createdConfig.getConfigurationId());
        assertTrue(configEntityOptional.isPresent());
    }

    @Test
    public void createConfigWithEmptyDescriptorNameTest() {
        createConfigWithEmptyDescriptorNameTestHelper(null);
        createConfigWithEmptyDescriptorNameTestHelper("");
    }

    @Test
    public void getConfigsByNameTest() throws AlertDatabaseConstraintException {
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField1));
        configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField2));

        List<ConfigurationModel> configurationsForDescriptor = configurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
        assertEquals(2, configurationsForDescriptor.size());
    }

    @Test
    public void getConfigurationByIdTest() throws AlertDatabaseConstraintException {
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel configurationModel1 = configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField1));
        ConfigurationModel configurationModel2 = configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField2));

        Optional<ConfigurationModel> optionalFoundConfig1 = configurationAccessor.getConfigurationById(configurationModel1.getConfigurationId());
        assertTrue(optionalFoundConfig1.isPresent());
        ConfigurationModel foundConfig1 = optionalFoundConfig1.get();
        assertEquals(configurationModel1.getDescriptorId(), foundConfig1.getDescriptorId());
        assertEquals(configurationModel1.getConfigurationId(), foundConfig1.getConfigurationId());

        Optional<ConfigurationModel> optionalFoundConfig2 = configurationAccessor.getConfigurationById(configurationModel2.getConfigurationId());
        assertTrue(optionalFoundConfig2.isPresent());
        ConfigurationModel foundConfig2 = optionalFoundConfig2.get();
        assertEquals(configurationModel2.getDescriptorId(), foundConfig2.getDescriptorId());
        assertEquals(configurationModel2.getConfigurationId(), foundConfig2.getConfigurationId());
    }

    @Test
    public void getConfigurationsWithNullIdTest() {
        try {
            configurationAccessor.getConfigurationById(null);
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("The config id cannot be null", e.getMessage());
        }
    }

    @Test
    public void getConfigurationsWithEmptyDescriptorNameTest() {
        getConfigByNameWithEmptyDescriptorNameTestHelper(null);
        getConfigByNameWithEmptyDescriptorNameTestHelper("");
    }

    @Test
    public void getConfigurationsByDescriptorTypeNullTest() {
        try {
            configurationAccessor.getConfigurationsByDescriptorType(null);
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("Descriptor type cannot be null", e.getMessage());
        }
    }

    @Test
    public void getConfigurationsByDescriptorTypeTest() throws Exception {
        List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationsByDescriptorType(DescriptorType.CHANNEL);
        assertTrue(configurationModels.isEmpty());
        ConfigurationFieldModel configField1 = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        ConfigurationFieldModel configField2 = ConfigurationFieldModel.createSensitive(FIELD_KEY_SENSITIVE);
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField1));
        configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(configField2));
        configurationModels = configurationAccessor.getConfigurationsByDescriptorType(DescriptorType.PROVIDER);
        assertFalse(configurationModels.isEmpty());
    }

    @Test
    public void getConfigurationsByFrequencyTypeNullTest() {
        try {
            configurationAccessor.getChannelConfigurationsByFrequency(null);
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("Frequency type cannot be null", e.getMessage());
        }
    }

    @Test
    public void getConfigurationsByFrequencyTypeTest() throws Exception {
        // No descriptor of Channel type registered
        List<ConfigurationModel> configurationModels = configurationAccessor.getChannelConfigurationsByFrequency(FrequencyType.DAILY);
        assertTrue(configurationModels.isEmpty());
        configurationModels = configurationAccessor.getChannelConfigurationsByFrequency(FrequencyType.REAL_TIME);
        assertTrue(configurationModels.isEmpty());
        ///////////////////////////////////////////

        final String descriptorName = "Unique Descriptor";
        DescriptorKey descriptorKey = createDescriptorKey(descriptorName);
        try {
            // Register a custom descriptor of Channel type
            descriptorMocker.registerDescriptor(descriptorName, DescriptorType.CHANNEL, Arrays.asList(new DefinedFieldModel(ChannelDistributionUIConfig.KEY_FREQUENCY, ConfigContextEnum.DISTRIBUTION, Boolean.FALSE)));

            configurationModels = configurationAccessor.getChannelConfigurationsByFrequency(FrequencyType.DAILY);
            assertTrue(configurationModels.isEmpty());
            configurationModels = configurationAccessor.getChannelConfigurationsByFrequency(FrequencyType.REAL_TIME);
            assertTrue(configurationModels.isEmpty());
            ////////////////////////////////////////////////

            // Add a configuration for the custom descriptor with a Frequency field
            ConfigurationFieldModel realTimeField = ConfigurationFieldModel.create(ChannelDistributionUIConfig.KEY_FREQUENCY);
            realTimeField.setFieldValue(FrequencyType.REAL_TIME.name());
            configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(realTimeField));

            configurationModels = configurationAccessor.getChannelConfigurationsByFrequency(FrequencyType.DAILY);
            assertTrue(configurationModels.isEmpty());
            configurationModels = configurationAccessor.getChannelConfigurationsByFrequency(FrequencyType.REAL_TIME);
            assertFalse(configurationModels.isEmpty());
            ////////////////////////////////////////////////////////////////////////
        } finally {
            descriptorMocker.unregisterDescriptor(descriptorName);
        }
    }

    @Test
    public void updateConfigurationMultipleValueTest() throws AlertDatabaseConstraintException {
        final String initialValue = "initial value";
        ConfigurationFieldModel originalField = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        originalField.setFieldValue(initialValue);
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel createdModel = configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(originalField));
        List<ConfigurationFieldModel> copyOfFieldList = createdModel.getCopyOfFieldList();
        assertEquals(1, copyOfFieldList.size());
        Optional<String> optionalValue = copyOfFieldList.get(0).getFieldValue();
        assertTrue(optionalValue.isPresent());
        assertEquals(initialValue, optionalValue.get());

        final String additionalValue = "additional value";
        ConfigurationFieldModel newFieldWithSameKey = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        newFieldWithSameKey.setFieldValue(additionalValue);

        ConfigurationModel updatedModel = configurationAccessor.updateConfiguration(createdModel.getConfigurationId(), Arrays.asList(originalField, newFieldWithSameKey));
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
    public void updateConfigurationReplaceValueTest() throws AlertDatabaseConstraintException {
        final String initialValue = "initial value";
        ConfigurationFieldModel originalField = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        originalField.setFieldValue(initialValue);

        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel createdModel = configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, Arrays.asList(originalField));
        List<ConfigurationFieldModel> copyOfFieldList = createdModel.getCopyOfFieldList();
        assertEquals(1, copyOfFieldList.size());
        Optional<String> optionalValue = copyOfFieldList.get(0).getFieldValue();
        assertTrue(optionalValue.isPresent());
        assertEquals(initialValue, optionalValue.get());

        final String additionalValue = "additional value";
        ConfigurationFieldModel newFieldWithSameKey = ConfigurationFieldModel.create(FIELD_KEY_INSENSITIVE);
        newFieldWithSameKey.setFieldValue(additionalValue);

        ConfigurationModel updatedModel = configurationAccessor.updateConfiguration(createdModel.getConfigurationId(), Arrays.asList(newFieldWithSameKey));
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
    public void updateConfigurationWithNullIdTest() {
        try {
            configurationAccessor.updateConfiguration(null, null);
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("The config id cannot be null", e.getMessage());
        }
    }

    @Test
    public void updateConfigurationWithInvalidIdTest() {
        final Long invalidId = Long.MAX_VALUE;
        try {
            configurationAccessor.updateConfiguration(invalidId, null);
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("A config with that id did not exist", e.getMessage());
        }
    }

    @Test
    public void updateConfigurationWithInvalidFieldKeyTest() throws Exception {
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel configurationModel = configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of());
        try {
            ConfigurationFieldModel field = ConfigurationFieldModel.create(null);
            configurationAccessor.updateConfiguration(configurationModel.getConfigurationId(), Arrays.asList(field));
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("Field key cannot be empty", e.getMessage());
        }
    }

    @Test
    public void deleteConfigurationTest() throws AlertDatabaseConstraintException {
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel createdModel1 = configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of());
        ConfigurationModel createdModel2 = configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of());
        List<ConfigurationModel> foundModels = configurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
        assertEquals(2, foundModels.size());

        configurationAccessor.deleteConfiguration(createdModel1);
        List<ConfigurationModel> afterFirstDeletion = configurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
        assertEquals(foundModels.size() - 1, afterFirstDeletion.size());

        configurationAccessor.deleteConfiguration(createdModel2);
        List<ConfigurationModel> afterSecondDeletion = configurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
        assertEquals(foundModels.size() - 2, afterSecondDeletion.size());
    }

    @Test
    public void deleteConfigurationWithInvalidArgsTest() {
        try {
            configurationAccessor.deleteConfiguration((ConfigurationModel) null);
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("Cannot delete a null object from the database", e.getMessage());
        }
        try {
            configurationAccessor.deleteConfiguration((Long) null);
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("The config id cannot be null", e.getMessage());
        }
    }

    @Test
    public void configurationModelTest() throws AlertDatabaseConstraintException {
        DescriptorKey descriptorKey = createDescriptorKey(DESCRIPTOR_NAME);
        ConfigurationModel configurationModel = configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of());
        assertNotNull(configurationModel.getConfigurationId());
        assertNotNull(configurationModel.getDescriptorId());
        assertNotNull(configurationModel.getCopyOfFieldList());
        assertNotNull(configurationModel.getCopyOfKeyToFieldMap());
    }

    private void createJobWithEmptyDescriptorNamesTestHelper(Collection<String> descriptorNames) {
        try {
            jobAccessor.createJob(descriptorNames, Set.of());
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertEquals("Descriptor names cannot be empty", e.getMessage());
        }
    }

    private void createConfigWithEmptyDescriptorNameTestHelper(String descriptorName) {
        try {
            DescriptorKey descriptorKey = createDescriptorKey(descriptorName);
            configurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.DISTRIBUTION, List.of());
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertTrue(e.getMessage().contains("DescriptorKey is not valid"), e.getMessage());
        }
    }

    private void getConfigByNameWithEmptyDescriptorNameTestHelper(String descriptorName) {
        try {
            DescriptorKey descriptorKey = createDescriptorKey(descriptorName);
            configurationAccessor.getConfigurationsByDescriptorKey(descriptorKey);
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertTrue(e.getMessage().contains("DescriptorKey is not valid"), e.getMessage());
        }
    }
}
