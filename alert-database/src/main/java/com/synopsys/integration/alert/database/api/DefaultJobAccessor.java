/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.database.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.datastructure.SetMap;

// @Component
// TODO eventually remove this class once tests are created for its replacement
@Deprecated(forRemoval = true)
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
    @Deprecated
    public List<ConfigurationJobModel> getAllJobs() {
        List<ConfigGroupEntity> jobEntities = configGroupRepository.findAll();
        return convertToJobModels(jobEntities);
    }

    @Override
    public List<ConfigurationJobModel> getMatchingEnabledJobs(FrequencyType frequency, Long providerConfigId, NotificationType notificationType) {
        //TODO change this to return a page of results
        return getMatchingEnabledJobs(() -> configGroupRepository.findMatchingEnabledJobIds(frequency.name(), String.valueOf(providerConfigId), notificationType.name()));
    }

    @Override
    public List<ConfigurationJobModel> getMatchingEnabledJobs(Long providerConfigId, NotificationType notificationType) {
        return getMatchingEnabledJobs(() -> configGroupRepository.findMatchingEnabledJobIds(String.valueOf(providerConfigId), notificationType.name()));
    }

    private List<ConfigurationJobModel> getMatchingEnabledJobs(Supplier<List<UUID>> getJobs) {
        //TODO change this to return a page of results
        List<UUID> matchingJobIds = getJobs.get();

        if (matchingJobIds.isEmpty()) {
            return List.of();
        }
        List<ConfigGroupEntity> jobEntities = configGroupRepository.findByJobIds(matchingJobIds);
        return convertToJobModels(jobEntities);
    }

    @Override
    public List<ConfigurationJobModel> getJobsById(Collection<UUID> jobIds) {
        List<ConfigGroupEntity> jobEntities = configGroupRepository.findAllByJobIdIn(jobIds);
        return convertToJobModels(jobEntities);
    }

    @Override
    public AlertPagedModel<ConfigurationJobModel> getPageOfJobs(int pageOffset, int pageLimit, Collection<String> descriptorsNamesToInclude) {
        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        Page<UUID> distinctJobIds = configGroupRepository.findDistinctJobIdsOnlyIncludingProvidedDescriptors(descriptorsNamesToInclude, pageRequest);
        List<ConfigGroupEntity> distinctJobs = configGroupRepository.findAllByJobIdIn(distinctJobIds.getContent());
        List<ConfigurationJobModel> jobModels = convertToJobModels(distinctJobs);
        return new AlertPagedModel<>(distinctJobIds.getTotalPages(), pageOffset, pageLimit, jobModels);
    }

    @Override
    public Optional<ConfigurationJobModel> getJobById(UUID jobId) {
        if (null == jobId) {
            return Optional.empty();
        }
        return executeQueryAndConvertToJobModel(() -> configGroupRepository.findByJobId(jobId));
    }

    @Override
    public Optional<ConfigurationJobModel> getJobByName(String jobName) {
        if (StringUtils.isBlank(jobName)) {
            return Optional.empty();
        }
        return executeQueryAndConvertToJobModel(() -> configGroupRepository.findByJobName(jobName));
    }

    @Override
    // FIXME not only does this check frequency in memory rather than in the query, but it is also unpaged
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

    private Optional<ConfigurationJobModel> executeQueryAndConvertToJobModel(Supplier<List<ConfigGroupEntity>> query) {
        List<ConfigGroupEntity> jobConfigEntities = query.get();
        return jobConfigEntities.stream()
                   .findAny()
                   .map(configGroupEntity -> createJobModelFromExistingConfigs(configGroupEntity.getJobId(), jobConfigEntities));
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
