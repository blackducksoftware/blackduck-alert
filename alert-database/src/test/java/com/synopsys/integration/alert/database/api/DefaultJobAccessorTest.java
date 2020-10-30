package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.ConfigGroupEntity;
import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.ConfigGroupRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorTypeRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;

public class DefaultJobAccessorTest {
    private ConfigGroupRepository configGroupRepository;
    private DescriptorConfigRepository descriptorConfigRepository;
    private ConfigContextRepository configContextRepository;
    private FieldValueRepository fieldValueRepository;
    private DefinedFieldRepository definedFieldRepository;
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    private DescriptorTypeRepository descriptorTypeRepository;

    private final ConfigContextEnum configContextEnum = ConfigContextEnum.GLOBAL;
    private final String fieldValue = "testFieldValue";
    private final String fieldKey = "channel.common.name";

    @BeforeEach
    public void init() {
        configGroupRepository = Mockito.mock(ConfigGroupRepository.class);
        descriptorConfigRepository = Mockito.mock(DescriptorConfigRepository.class);
        configContextRepository = Mockito.mock(ConfigContextRepository.class);
        fieldValueRepository = Mockito.mock(FieldValueRepository.class);
        definedFieldRepository = Mockito.mock(DefinedFieldRepository.class);
        registeredDescriptorRepository = Mockito.mock(RegisteredDescriptorRepository.class);
        descriptorTypeRepository = Mockito.mock(DescriptorTypeRepository.class);
    }

    @Test
    public void getAllJobsTest() {
        UUID uuid = UUID.randomUUID();

        ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(1L, uuid);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(2L, 2L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(3L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(3L, 4L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(4L);

        Mockito.when(configGroupRepository.findAll()).thenReturn(List.of(configGroupEntity));
        setupGetJobMocks(descriptorConfigEntity, configContextEntity, fieldValueEntity, definedFieldEntity);

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(
            null, null, definedFieldRepository, descriptorConfigRepository, configContextRepository, fieldValueRepository, null);
        DefaultJobAccessor jobAccessor = new DefaultJobAccessor(configGroupRepository, configurationAccessor);
        List<ConfigurationJobModel> configurationJobModelList = jobAccessor.getAllJobs();

        assertEquals(1, configurationJobModelList.size());
        ConfigurationJobModel configurationJobModel = configurationJobModelList.get(0);
        assertEquals(uuid, configurationJobModel.getJobId());
        assertEquals(fieldValue, configurationJobModel.getName());
    }

    @Test
    public void getJobByIdTest() throws Exception {
        UUID jobId = UUID.randomUUID();

        ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(1L, jobId);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(2L, 2L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(3L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(3L, 4L, fieldValue);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(4L);

        Mockito.when(configGroupRepository.findByJobId(Mockito.any())).thenReturn(List.of(configGroupEntity));
        setupGetJobMocks(descriptorConfigEntity, configContextEntity, fieldValueEntity, definedFieldEntity);

        DefaultConfigurationAccessor configurationAccessor =
            new DefaultConfigurationAccessor(null, null, definedFieldRepository, descriptorConfigRepository, configContextRepository, fieldValueRepository, null);
        DefaultJobAccessor jobAccessor = new DefaultJobAccessor(configGroupRepository, configurationAccessor);
        Optional<ConfigurationJobModel> configurationJobModelOptional = jobAccessor.getJobById(jobId);

        assertTrue(configurationJobModelOptional.isPresent());
        ConfigurationJobModel configurationJobModel = configurationJobModelOptional.get();
        assertEquals(jobId, configurationJobModel.getJobId());
        assertEquals(fieldValue, configurationJobModel.getName());
    }

    @Test
    public void getJobsByFrequency() {
        FrequencyType frequencyType = FrequencyType.DAILY;
        final Long jobId = 1L;
        UUID uuid = UUID.randomUUID();
        String fieldValueFrequency = frequencyType.name();
        final String fieldKeyFrequency = "channel.common.frequency";

        ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(jobId, uuid);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(2L, 2L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(3L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        FieldValueEntity fieldValueEntity = new FieldValueEntity(3L, 4L, fieldValueFrequency);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKeyFrequency, false);
        definedFieldEntity.setId(4L);

        Mockito.when(configGroupRepository.findAll()).thenReturn(List.of(configGroupEntity));
        setupGetJobMocks(descriptorConfigEntity, configContextEntity, fieldValueEntity, definedFieldEntity);

        DefaultConfigurationAccessor configurationAccessor =
            new DefaultConfigurationAccessor(null, null, definedFieldRepository, descriptorConfigRepository, configContextRepository, fieldValueRepository, null);
        DefaultJobAccessor jobAccessor = new DefaultJobAccessor(configGroupRepository, configurationAccessor);
        List<ConfigurationJobModel> configurationJobModelList = jobAccessor.getJobsByFrequency(frequencyType);

        assertEquals(1, configurationJobModelList.size());
        ConfigurationJobModel configurationJobModel = configurationJobModelList.get(0);
        assertEquals(uuid, configurationJobModel.getJobId());
        assertEquals(fieldValueFrequency, configurationJobModel.getFrequencyType().name());
    }

    @Test
    public void getJobByIdNullTest() {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        DefaultJobAccessor jobAccessor = new DefaultJobAccessor(configGroupRepository, configurationAccessor);
        Optional<ConfigurationJobModel> jobById = jobAccessor.getJobById(null);
        assertTrue(jobById.isEmpty(), "Expected no job with a null id to be found");
    }

    @Test
    public void createJobTest() throws Exception {
        List<String> descriptorNames = List.of("descriptor-name-test");

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create("channel.common.name");
        configurationFieldModel.setFieldValue(fieldValue);
        List<ConfigurationFieldModel> configuredFields = List.of(configurationFieldModel);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name", 1L);
        registeredDescriptorEntity.setId(2L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        configContextEntity.setId(3L);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(4L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(5L, 6L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(5L);

        setupCreateJobMocks(registeredDescriptorEntity, configContextEntity, definedFieldEntity, descriptorConfigEntity);

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, null, definedFieldRepository, descriptorConfigRepository, configContextRepository,
            fieldValueRepository, null);
        DefaultJobAccessor jobAccessor = new DefaultJobAccessor(configGroupRepository, configurationAccessor);
        ConfigurationJobModel configurationJobModel = jobAccessor.createJob(descriptorNames, configuredFields);

        assertEquals(fieldValue, configurationJobModel.getName());
    }

    @Test
    public void updateJobTest() throws Exception {
        UUID uuid = UUID.randomUUID();
        List<String> descriptorNames = List.of("descriptor-name-test");

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create("channel.common.name");
        configurationFieldModel.setFieldValue(fieldValue);
        List<ConfigurationFieldModel> configuredFields = List.of(configurationFieldModel);
        RegisteredDescriptorEntity registeredDescriptorEntity = new RegisteredDescriptorEntity("name", 1L);
        registeredDescriptorEntity.setId(2L);
        ConfigContextEntity configContextEntity = new ConfigContextEntity(configContextEnum.name());
        configContextEntity.setId(3L);
        DefinedFieldEntity definedFieldEntity = new DefinedFieldEntity(fieldKey, false);
        definedFieldEntity.setId(4L);
        DescriptorConfigEntity descriptorConfigEntity = new DescriptorConfigEntity(5L, 6L, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        descriptorConfigEntity.setId(5L);
        ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(6L, uuid);

        Mockito.when(configGroupRepository.findByJobId(Mockito.any())).thenReturn(List.of(configGroupEntity));
        setupCreateJobMocks(registeredDescriptorEntity, configContextEntity, definedFieldEntity, descriptorConfigEntity);

        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(registeredDescriptorRepository, null, definedFieldRepository, descriptorConfigRepository, configContextRepository,
            fieldValueRepository, null);
        JobAccessor jobAccessor = new DefaultJobAccessor(configGroupRepository, configurationAccessor);
        ConfigurationJobModel configurationJobModel = jobAccessor.updateJob(uuid, descriptorNames, configuredFields);

        Mockito.verify(descriptorConfigRepository).deleteById(Mockito.any());
        assertEquals(uuid, configurationJobModel.getJobId());
        assertEquals(fieldValue, configurationJobModel.getName());
    }

    @Test
    public void updateJobNullIdTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        JobAccessor jobAccessor = new DefaultJobAccessor(configGroupRepository, configurationAccessor);

        try {
            jobAccessor.updateJob(null, null, null);
            fail("Null jobId did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void deleteJobNullIdTest() throws Exception {
        DefaultConfigurationAccessor configurationAccessor = new DefaultConfigurationAccessor(null, null, null, null, null, null, null);
        JobAccessor jobAccessor = new DefaultJobAccessor(configGroupRepository, configurationAccessor);

        try {
            jobAccessor.deleteJob(null);
            fail("Null jobId did not throw expected AlertDatabaseConstraintException.");
        } catch (AlertDatabaseConstraintException e) {
            assertNotNull(e);
        }
    }

    private void setupGetJobMocks(DescriptorConfigEntity descriptorConfigEntity, ConfigContextEntity configContextEntity, FieldValueEntity fieldValueEntity, DefinedFieldEntity definedFieldEntity) {
        Mockito.when(descriptorConfigRepository.findById(Mockito.any())).thenReturn(Optional.of(descriptorConfigEntity));
        Mockito.when(configContextRepository.findById(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(fieldValueRepository.findByConfigId(Mockito.any())).thenReturn(List.of(fieldValueEntity));
        Mockito.when(definedFieldRepository.findById(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));
    }

    private void setupCreateJobMocks(RegisteredDescriptorEntity registeredDescriptorEntity, ConfigContextEntity configContextEntity, DefinedFieldEntity definedFieldEntity, DescriptorConfigEntity descriptorConfigEntity) {
        Mockito.when(registeredDescriptorRepository.findFirstByName(Mockito.any())).thenReturn(Optional.of(registeredDescriptorEntity));
        Mockito.when(configContextRepository.findFirstByContext(Mockito.any())).thenReturn(Optional.of(configContextEntity));
        Mockito.when(definedFieldRepository.findByDescriptorIdAndContext(Mockito.any(), Mockito.any())).thenReturn(List.of(definedFieldEntity));
        Mockito.when(descriptorConfigRepository.save(Mockito.any())).thenReturn(descriptorConfigEntity);
        Mockito.when(definedFieldRepository.findFirstByKey(Mockito.any())).thenReturn(Optional.of(definedFieldEntity));
    }

}
