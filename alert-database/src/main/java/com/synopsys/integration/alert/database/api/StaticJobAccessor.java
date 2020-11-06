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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.database.job.DistributionJobEntity;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.database.job.JobConfigurationModelFieldExtractorUtils;
import com.synopsys.integration.alert.database.job.JobConfigurationModelFieldPopulationUtility;
import com.synopsys.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;
import com.synopsys.integration.alert.database.job.email.EmailJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.email.EmailJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsEntity;
import com.synopsys.integration.alert.database.job.msteams.MSTeamsJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.msteams.MSTeamsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.slack.SlackJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.slack.SlackJobDetailsEntity;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class StaticJobAccessor implements JobAccessor {
    private final DistributionJobRepository distributionJobRepository;
    private final BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor;
    private final AzureBoardsJobDetailsAccessor azureBoardsJobDetailsAccessor;
    private final EmailJobDetailsAccessor emailJobDetailsAccessor;
    private final JiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor;
    private final JiraServerJobDetailsAccessor jiraServerJobDetailsAccessor;
    private final MSTeamsJobDetailsAccessor msTeamsJobDetailsAccessor;
    private final SlackJobDetailsAccessor slackJobDetailsAccessor;

    // Temporary until all three tiers of the application have been updated to new Job models
    private final JobConfigurationModelFieldPopulationUtility jobConfigurationModelFieldPopulationUtility;
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    // BlackDuck is currently the only provider, so this is safe in the short-term while we transition to new models
    private final ProviderKey blackDuckProviderKey;

    @Autowired
    public StaticJobAccessor(
        DistributionJobRepository distributionJobRepository,
        BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor,
        AzureBoardsJobDetailsAccessor azureBoardsJobDetailsAccessor,
        EmailJobDetailsAccessor emailJobDetailsAccessor,
        JiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor,
        JiraServerJobDetailsAccessor jiraServerJobDetailsAccessor,
        MSTeamsJobDetailsAccessor msTeamsJobDetailsAccessor,
        SlackJobDetailsAccessor slackJobDetailsAccessor,
        JobConfigurationModelFieldPopulationUtility jobConfigurationModelFieldPopulationUtility,
        RegisteredDescriptorRepository registeredDescriptorRepository,
        ProviderKey blackDuckProviderKey
    ) {
        this.distributionJobRepository = distributionJobRepository;
        this.blackDuckJobDetailsAccessor = blackDuckJobDetailsAccessor;
        this.azureBoardsJobDetailsAccessor = azureBoardsJobDetailsAccessor;
        this.emailJobDetailsAccessor = emailJobDetailsAccessor;
        this.jiraCloudJobDetailsAccessor = jiraCloudJobDetailsAccessor;
        this.jiraServerJobDetailsAccessor = jiraServerJobDetailsAccessor;
        this.msTeamsJobDetailsAccessor = msTeamsJobDetailsAccessor;
        this.slackJobDetailsAccessor = slackJobDetailsAccessor;
        this.jobConfigurationModelFieldPopulationUtility = jobConfigurationModelFieldPopulationUtility;
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.blackDuckProviderKey = blackDuckProviderKey;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigurationJobModel> getAllJobs() {
        return distributionJobRepository.findAll()
                   .stream()
                   .map(this::convertToConfigurationJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigurationJobModel> getJobsById(Collection<UUID> jobIds) {
        return distributionJobRepository.findAllById(jobIds)
                   .stream()
                   .map(this::convertToConfigurationJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<ConfigurationJobModel> getPageOfJobs(int pageNumber, int pageLimit) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageLimit);
        Page<ConfigurationJobModel> pageOfJobsWithDescriptorNames = distributionJobRepository.findAll(pageRequest).map(this::convertToConfigurationJobModel);
        return new AlertPagedModel<>(pageOfJobsWithDescriptorNames.getTotalPages(), pageNumber, pageLimit, pageOfJobsWithDescriptorNames.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<ConfigurationJobModel> getPageOfJobs(int pageNumber, int pageLimit, Collection<String> descriptorsNamesToInclude) {
        if (!descriptorsNamesToInclude.contains(blackDuckProviderKey.getUniversalKey())) {
            return new AlertPagedModel<>(0, pageNumber, pageLimit, List.of());
        }

        PageRequest pageRequest = PageRequest.of(pageNumber, pageLimit);
        Page<ConfigurationJobModel> pageOfJobsWithDescriptorNames = distributionJobRepository.findByChannelDescriptorNameIn(descriptorsNamesToInclude, pageRequest)
                                                                        .map(this::convertToConfigurationJobModel);
        return new AlertPagedModel<>(pageOfJobsWithDescriptorNames.getTotalPages(), pageNumber, pageLimit, pageOfJobsWithDescriptorNames.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfigurationJobModel> getJobById(UUID jobId) {
        return distributionJobRepository.findById(jobId).map(this::convertToConfigurationJobModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfigurationJobModel> getJobByName(String jobName) {
        return distributionJobRepository.findByName(jobName)
                   .map(this::convertToConfigurationJobModel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigurationJobModel> getJobsByFrequency(FrequencyType frequency) {
        return distributionJobRepository.findByDistributionFrequency(frequency.name())
                   .stream()
                   .map(this::convertToConfigurationJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    public List<ConfigurationJobModel> getMatchingEnabledJobs(FrequencyType frequency, Long providerConfigId, NotificationType notificationType) {
        // TODO change this to return a page of jobs
        return getMatchingEnabledJobs(() -> distributionJobRepository.findMatchingEnabledJob(frequency.name(), providerConfigId, notificationType.name()));
    }

    @Override
    public List<ConfigurationJobModel> getMatchingEnabledJobs(Long providerConfigId, NotificationType notificationType) {
        // TODO change this to return a page of jobs
        return getMatchingEnabledJobs(() -> distributionJobRepository.findMatchingEnabledJob(providerConfigId, notificationType.name()));
    }

    private List<ConfigurationJobModel> getMatchingEnabledJobs(Supplier<List<DistributionJobEntity>> getJobs) {
        // TODO change this to return a page of jobs
        List<DistributionJobEntity> matchingEnabledJob = getJobs.get();
        return matchingEnabledJob
                   .stream()
                   .map(this::convertToConfigurationJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConfigurationJobModel createJob(Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) {
        return createJobWithId(null, configuredFields, DateUtils.createCurrentDateTimestamp(), null);
    }

    @Override
    @Transactional
    public ConfigurationJobModel updateJob(UUID jobId, Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
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

    private ConfigurationJobModel createJobWithId(UUID jobId, Collection<ConfigurationFieldModel> configuredFields, OffsetDateTime createdAt, OffsetDateTime lastUpdated) {
        Map<String, ConfigurationFieldModel> configuredFieldsMap = configuredFields
                                                                       .stream()
                                                                       .collect(Collectors.toMap(ConfigurationFieldModel::getFieldKey, Function.identity()));

        DistributionJobModel distributionJobModel = JobConfigurationModelFieldExtractorUtils.convertToDistributionJobModel(jobId, configuredFieldsMap, createdAt, lastUpdated);
        return createJob(distributionJobModel);
    }

    private ConfigurationJobModel createJob(DistributionJobModel distributionJobModel) {
        String channelDescriptorName = distributionJobModel.getChannelDescriptorName();
        DistributionJobEntity jobToSave = new DistributionJobEntity(
            distributionJobModel.getJobId(),
            distributionJobModel.getName(),
            distributionJobModel.isEnabled(),
            distributionJobModel.getDistributionFrequency().name(),
            distributionJobModel.getProcessingType().name(),
            channelDescriptorName,
            distributionJobModel.getCreatedAt(),
            distributionJobModel.getLastUpdated().orElse(null)
        );
        DistributionJobEntity savedJobEntity = distributionJobRepository.save(jobToSave);
        UUID savedJobId = savedJobEntity.getJobId();

        BlackDuckJobDetailsEntity savedBlackDuckJobDetails = blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(savedJobId, distributionJobModel);
        savedJobEntity.setBlackDuckJobDetails(savedBlackDuckJobDetails);

        DistributionJobDetailsModel distributionJobDetails = distributionJobModel.getDistributionJobDetails();
        if (distributionJobDetails.isAzureBoardsDetails()) {
            AzureBoardsJobDetailsEntity savedAzureBoardsJobDetails = azureBoardsJobDetailsAccessor.saveAzureBoardsJobDetails(savedJobId, distributionJobDetails.getAsAzureBoardsJobDetails());
            savedJobEntity.setAzureBoardsJobDetails(savedAzureBoardsJobDetails);
        } else if (distributionJobDetails.isEmailDetails()) {
            EmailJobDetailsEntity savedEmailJobDetails = emailJobDetailsAccessor.saveEmailJobDetails(savedJobId, distributionJobDetails.getAsEmailJobDetails());
            savedJobEntity.setEmailJobDetails(savedEmailJobDetails);
        } else if (distributionJobDetails.isJiraCloudDetails()) {
            JiraCloudJobDetailsEntity savedJiraCloudJobDetails = jiraCloudJobDetailsAccessor.saveJiraCloudJobDetails(savedJobId, distributionJobDetails.getAsJiraCouldJobDetails());
            savedJobEntity.setJiraCloudJobDetails(savedJiraCloudJobDetails);
        } else if (distributionJobDetails.isJiraServerDetails()) {
            JiraServerJobDetailsEntity savedJiraServerJobDetails = jiraServerJobDetailsAccessor.saveJiraServerJobDetails(savedJobId, distributionJobDetails.getAsJiraServerJobDetails());
            savedJobEntity.setJiraServerJobDetails(savedJiraServerJobDetails);
        } else if (distributionJobDetails.isMSTeamsDetails()) {
            MSTeamsJobDetailsEntity savedMSTeamsJobDetails = msTeamsJobDetailsAccessor.saveMSTeamsJobDetails(savedJobId, distributionJobDetails.getAsMSTeamsJobDetails());
            savedJobEntity.setMsTeamsJobDetails(savedMSTeamsJobDetails);
        } else if (distributionJobDetails.isSlackDetails()) {
            SlackJobDetailsEntity savedSlackJobDetails = slackJobDetailsAccessor.saveSlackJobDetails(savedJobId, distributionJobDetails.getAsSlackJobDetails());
            savedJobEntity.setSlackJobDetails(savedSlackJobDetails);
        }

        return convertToConfigurationJobModel(savedJobEntity);
    }

}
