package com.synopsys.integration.alert.database.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.database.configuration.ConfigGroupEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigGroupRepository;
import com.synopsys.integration.datastructure.SetMap;

@Component
public class DefaultJobAccessor implements JobAccessor {
    public static final String NULL_JOB_ID = "The job id cannot be null";

    private final ConfigGroupRepository configGroupRepository;
    private final DefaultConfigurationAccessor configurationAccessor;

    @Autowired
    public DefaultJobAccessor(ConfigGroupRepository configGroupRepository, DefaultConfigurationAccessor configurationAccessor) {
        this.configGroupRepository = configGroupRepository;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public List<ConfigurationJobModel> getAllJobs() {
        List<ConfigGroupEntity> jobEntities = configGroupRepository.findAll();
        return convertToJobModels(jobEntities);
    }

    @Override
    public AlertPagedModel<ConfigurationJobModel> getPageOfJobs(PageRequest pageRequest) {
        Page<ConfigGroupEntity> jobsWithDistinctIds = configGroupRepository.findJobsWithDistinctIds(pageRequest);
        List<ConfigurationJobModel> jobModels = convertToJobModels(jobsWithDistinctIds);
        return new AlertPagedModel<>(jobsWithDistinctIds, jobModels);
    }

    @Override
    public Optional<ConfigurationJobModel> getJobById(UUID jobId) throws AlertDatabaseConstraintException {
        if (jobId == null) {
            throw new AlertDatabaseConstraintException(NULL_JOB_ID);
        }
        List<ConfigGroupEntity> jobConfigEntities = configGroupRepository.findByJobId(jobId);
        return jobConfigEntities
                   .stream()
                   .findAny()
                   .map(configGroupEntity -> createJobModelFromExistingConfigs(configGroupEntity.getJobId(), jobConfigEntities));
    }

    @Override
    public List<ConfigurationJobModel> getJobsByFrequency(FrequencyType frequency) {
        return getAllJobs()
                   .stream()
                   .filter(job -> frequency == job.getFrequencyType())
                   .collect(Collectors.toList());
    }

    @Override
    public ConfigurationJobModel createJob(Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        return createJob(null, descriptorNames, configuredFields);
    }

    @Override

    public ConfigurationJobModel updateJob(UUID jobId, Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (jobId == null) {
            throw new AlertDatabaseConstraintException(NULL_JOB_ID);
        }

        deleteJob(jobId);
        return createJob(jobId, descriptorNames, configuredFields);
    }

    @Override
    public void deleteJob(UUID jobId) throws AlertDatabaseConstraintException {
        if (jobId == null) {
            throw new AlertDatabaseConstraintException(NULL_JOB_ID);
        }
        List<Long> configIdsForJob = configGroupRepository
                                         .findByJobId(jobId)
                                         .stream()
                                         .map(ConfigGroupEntity::getConfigId)
                                         .collect(Collectors.toList());
        for (Long configId : configIdsForJob) {
            configurationAccessor.deleteConfiguration(configId);
        }
    }

    private List<ConfigurationJobModel> convertToJobModels(Iterable<ConfigGroupEntity> jobEntities) {
        SetMap<UUID, ConfigGroupEntity> jobMap = SetMap.createDefault();
        for (ConfigGroupEntity entity : jobEntities) {
            UUID entityJobId = entity.getJobId();
            jobMap.add(entityJobId, entity);
        }

        return jobMap.entrySet()
                   .stream()
                   .map(entry -> createJobModelFromExistingConfigs(entry.getKey(), entry.getValue()))
                   .collect(Collectors.toList());
    }

    private ConfigurationJobModel createJob(UUID oldJobId, Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        if (descriptorNames == null || descriptorNames.isEmpty()) {
            throw new AlertDatabaseConstraintException("Descriptor names cannot be empty");
        }
        Set<ConfigurationModel> configurationModels = new HashSet<>();
        for (String descriptorName : descriptorNames) {
            configurationModels.add(configurationAccessor.createConfigForRelevantFields(descriptorName, configuredFields));
        }

        UUID newJobId = oldJobId;
        if (newJobId == null) {
            newJobId = UUID.randomUUID();
        }

        List<ConfigGroupEntity> configGroupsToSave = new ArrayList<>(configurationModels.size());
        for (ConfigurationModel createdModel : configurationModels) {
            ConfigGroupEntity configGroupEntityToSave = new ConfigGroupEntity(createdModel.getConfigurationId(), newJobId);
            configGroupsToSave.add(configGroupEntityToSave);
        }
        configGroupRepository.saveAll(configGroupsToSave);
        return new ConfigurationJobModel(newJobId, configurationModels);
    }

    private ConfigurationJobModel createJobModelFromExistingConfigs(UUID jobId, Collection<ConfigGroupEntity> entities) {
        Set<ConfigurationModel> configurationModels = new HashSet<>();
        for (ConfigGroupEntity sortedEntity : entities) {
            try {
                configurationAccessor.getConfigurationById(sortedEntity.getConfigId()).ifPresent(configurationModels::add);
            } catch (AlertDatabaseConstraintException e) {
                // This case should be impossible based on database constraints
                throw new AlertRuntimeException(e);
            }
        }
        return new ConfigurationJobModel(jobId, configurationModels);
    }

}
