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

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessorV2;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.database.job.DistributionJobEntity;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.database.job.JobConfigurationModelFieldExtractorUtils;
import com.synopsys.integration.alert.database.job.JobConfigurationModelFieldPopulationUtility;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class StaticJobAccessorV2 implements JobAccessorV2 {
    private final DistributionJobRepository distributionJobRepository;

    // Temporary until all three tiers of the application have been updated to new Job models
    private final JobConfigurationModelFieldPopulationUtility jobConfigurationModelFieldPopulationUtility;
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    // BlackDuck is currently the only provider, so this is safe in the short-term while we transition to new models
    private final ProviderKey blackDuckProviderKey;

    @Autowired
    public StaticJobAccessorV2(
        DistributionJobRepository distributionJobRepository,
        JobConfigurationModelFieldPopulationUtility jobConfigurationModelFieldPopulationUtility,
        RegisteredDescriptorRepository registeredDescriptorRepository,
        ProviderKey blackDuckProviderKey
    ) {
        this.distributionJobRepository = distributionJobRepository;
        this.jobConfigurationModelFieldPopulationUtility = jobConfigurationModelFieldPopulationUtility;
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.blackDuckProviderKey = blackDuckProviderKey;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistributionJobModel> getJobsById(Collection<UUID> jobIds) {
        return distributionJobRepository.findAllById(jobIds)
                   .stream()
                   .map(this::convertToDistributionJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<DistributionJobModel> getPageOfJobs(int pageNumber, int pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageLimit);
        Page<DistributionJobModel> pageOfJobsWithDescriptorNames = distributionJobRepository.findAll(pageRequest).map(this::convertToDistributionJobModel);
        return new AlertPagedModel<>(pageOfJobsWithDescriptorNames.getTotalPages(), pageNumber, pageLimit, pageOfJobsWithDescriptorNames.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<DistributionJobModel> getPageOfJobs(int pageNumber, int pageLimit, String searchTerm, Collection<String> descriptorsNamesToInclude) {
        if (!descriptorsNamesToInclude.contains(blackDuckProviderKey.getUniversalKey())) {
            return new AlertPagedModel<>(0, pageNumber, pageLimit, List.of());
        }

        PageRequest pageRequest = PageRequest.of(pageNumber, pageLimit);
        Page<DistributionJobEntity> pageOfJobsWithDescriptorNames;
        if (StringUtils.isBlank(searchTerm)) {
            pageOfJobsWithDescriptorNames = distributionJobRepository.findByChannelDescriptorNameIn(descriptorsNamesToInclude, pageRequest);
        } else {
            pageOfJobsWithDescriptorNames = distributionJobRepository.findByChannelDescriptorNamesAndSearchTerm(descriptorsNamesToInclude, searchTerm, pageRequest);
        }

        List<DistributionJobModel> configurationJobModels = pageOfJobsWithDescriptorNames.map(this::convertToDistributionJobModel).getContent();
        return new AlertPagedModel<>(pageOfJobsWithDescriptorNames.getTotalPages(), pageNumber, pageLimit, configurationJobModels);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DistributionJobModel> getJobById(UUID jobId) {
        return distributionJobRepository.findById(jobId).map(this::convertToDistributionJobModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DistributionJobModel> getJobByName(String jobName) {
        return distributionJobRepository.findByName(jobName)
                   .map(this::convertToDistributionJobModel);
    }

    @Override
    public List<DistributionJobModel> getMatchingEnabledJobs(FrequencyType frequency, Long providerConfigId, NotificationType notificationType) {
        // TODO change this to return a page of jobs
        return getMatchingEnabledJobs(() -> distributionJobRepository.findMatchingEnabledJob(frequency.name(), providerConfigId, notificationType.name()));
    }

    @Override
    public List<DistributionJobModel> getMatchingEnabledJobs(Long providerConfigId, NotificationType notificationType) {
        // TODO change this to return a page of jobs
        return getMatchingEnabledJobs(() -> distributionJobRepository.findMatchingEnabledJob(providerConfigId, notificationType.name()));
    }

    private List<DistributionJobModel> getMatchingEnabledJobs(Supplier<List<DistributionJobEntity>> getJobs) {
        // TODO change this to return a page of jobs
        List<DistributionJobEntity> matchingEnabledJob = getJobs.get();
        return matchingEnabledJob
                   .stream()
                   .map(this::convertToDistributionJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DistributionJobModel createJob(Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) {
        return createJobWithId(null, configuredFields, DateUtils.createCurrentDateTimestamp(), null);
    }

    @Override
    @Transactional
    public DistributionJobModel updateJob(UUID jobId, Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        DistributionJobEntity jobEntity = distributionJobRepository.findById(jobId)
                                              .orElseThrow(() -> new AlertDatabaseConstraintException(String.format("No job exists with the id [%s]", jobId.toString())));
        OffsetDateTime createdAt = jobEntity.getCreatedAt();

        deleteJob(jobId);
        return createJobWithId(jobId, configuredFields, createdAt, DateUtils.createCurrentDateTimestamp());
    }

    @Override
    @Transactional
    public void deleteJob(UUID jobId) {
        distributionJobRepository.deleteById(jobId);
    }

    private DistributionJobModel convertToDistributionJobModel(DistributionJobEntity jobEntity) {
        ConfigurationJobModel configurationJobModel = convertToConfigurationJobModel(jobEntity);
        Map<String, ConfigurationFieldModel> fields = configurationJobModel.getFieldUtility().getFields();
        return JobConfigurationModelFieldExtractorUtils.convertToDistributionJobModel(configurationJobModel.getJobId(), fields, jobEntity.getCreatedAt(), jobEntity.getLastUpdated());
    }

    private ConfigurationJobModel convertToConfigurationJobModel(DistributionJobEntity jobEntity) {
        Set<ConfigurationModel> configurationModels = new LinkedHashSet<>();

        String createdAtDateTime = DateUtils.formatDate(jobEntity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String updatedAtDateTime = Optional.ofNullable(jobEntity.getLastUpdated())
                                       .map(date -> DateUtils.formatDate(date, DateUtils.UTC_DATE_FORMAT_TO_MINUTE))
                                       .orElse(null);

        String providerUniversalKey = blackDuckProviderKey.getUniversalKey();
        Long blackDuckDescriptorId = getDescriptorId(providerUniversalKey);

        ConfigurationModelMutable blackDuckConfigurationModel = new ConfigurationModelMutable(blackDuckDescriptorId, -1L, createdAtDateTime, updatedAtDateTime, ConfigContextEnum.DISTRIBUTION);
        jobConfigurationModelFieldPopulationUtility.populateBlackDuckConfigurationModelFields(jobEntity, blackDuckConfigurationModel);
        configurationModels.add(blackDuckConfigurationModel);

        Long channelDescriptorId = getDescriptorId(jobEntity.getChannelDescriptorName());
        ConfigurationModelMutable channelConfigurationModel = new ConfigurationModelMutable(channelDescriptorId, -1L, createdAtDateTime, updatedAtDateTime, ConfigContextEnum.DISTRIBUTION);
        channelConfigurationModel.put(jobConfigurationModelFieldPopulationUtility.createConfigFieldModel("channel.common.provider.name", providerUniversalKey));
        jobConfigurationModelFieldPopulationUtility.populateChannelConfigurationModelFields(jobEntity, channelConfigurationModel);
        configurationModels.add(channelConfigurationModel);

        return new ConfigurationJobModel(jobEntity.getJobId(), configurationModels);
    }

    private Long getDescriptorId(String descriptorUniversalKey) {
        return registeredDescriptorRepository.findFirstByName(descriptorUniversalKey)
                   .map(RegisteredDescriptorEntity::getId)
                   .orElseThrow(() -> new AlertRuntimeException("A descriptor is missing from the database"));
    }

    private DistributionJobModel createJobWithId(UUID jobId, Collection<ConfigurationFieldModel> configuredFields, OffsetDateTime createdAt, OffsetDateTime lastUpdated) {
        Map<String, ConfigurationFieldModel> configuredFieldsMap = configuredFields
                                                                       .stream()
                                                                       .collect(Collectors.toMap(ConfigurationFieldModel::getFieldKey, Function.identity()));

        return JobConfigurationModelFieldExtractorUtils.convertToDistributionJobModel(jobId, configuredFieldsMap, createdAt, lastUpdated);
    }

}
