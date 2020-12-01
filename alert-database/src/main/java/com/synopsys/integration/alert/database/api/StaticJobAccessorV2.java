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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessorV2;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.job.DistributionJobEntity;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;
import com.synopsys.integration.alert.database.job.email.EmailJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.email.EmailJobDetailsEntity;
import com.synopsys.integration.alert.database.job.email.additional.EmailJobAdditionalEmailAddressEntity;
import com.synopsys.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsEntity;
import com.synopsys.integration.alert.database.job.msteams.MSTeamsJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.msteams.MSTeamsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.slack.SlackJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.slack.SlackJobDetailsEntity;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class StaticJobAccessorV2 implements JobAccessorV2 {
    private final DistributionJobRepository distributionJobRepository;
    private final BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor;
    private final AzureBoardsJobDetailsAccessor azureBoardsJobDetailsAccessor;
    private final EmailJobDetailsAccessor emailJobDetailsAccessor;
    private final JiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor;
    private final JiraServerJobDetailsAccessor jiraServerJobDetailsAccessor;
    private final MSTeamsJobDetailsAccessor msTeamsJobDetailsAccessor;
    private final SlackJobDetailsAccessor slackJobDetailsAccessor;

    // Temporary until all three tiers of the application have been updated to new Job models
    // BlackDuck is currently the only provider, so this is safe in the short-term while we transition to new models
    private final ProviderKey blackDuckProviderKey;

    @Autowired
    public StaticJobAccessorV2(DistributionJobRepository distributionJobRepository,
        BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor,
        AzureBoardsJobDetailsAccessor azureBoardsJobDetailsAccessor,
        EmailJobDetailsAccessor emailJobDetailsAccessor,
        JiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor,
        JiraServerJobDetailsAccessor jiraServerJobDetailsAccessor,
        MSTeamsJobDetailsAccessor msTeamsJobDetailsAccessor,
        SlackJobDetailsAccessor slackJobDetailsAccessor,
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
    @Transactional(readOnly = true)
    public List<DistributionJobModel> getMatchingEnabledJobs(FrequencyType frequency, Long providerConfigId, NotificationType notificationType) {
        // TODO change this to return a page of jobs
        return getMatchingEnabledJobs(() -> distributionJobRepository.findMatchingEnabledJob(frequency.name(), providerConfigId, notificationType.name()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistributionJobModel> getMatchingEnabledJobs(Long providerConfigId, NotificationType notificationType) {
        // TODO change this to return a page of jobs
        return getMatchingEnabledJobs(() -> distributionJobRepository.findMatchingEnabledJob(providerConfigId, notificationType.name()));
    }

    @Override
    @Transactional
    public DistributionJobModel createJob(DistributionJobRequestModel requestModel) {
        return createJobWithId(null, requestModel, DateUtils.createCurrentDateTimestamp(), null);
    }

    @Override
    @Transactional
    public DistributionJobModel updateJob(UUID jobId, DistributionJobRequestModel requestModel) throws AlertDatabaseConstraintException {
        DistributionJobEntity jobEntity = distributionJobRepository.findById(jobId)
                                              .orElseThrow(() -> new AlertDatabaseConstraintException(String.format("No job exists with the id [%s]", jobId.toString())));
        OffsetDateTime createdAt = jobEntity.getCreatedAt();

        deleteJob(jobId);
        return createJobWithId(jobId, requestModel, createdAt, DateUtils.createCurrentDateTimestamp());
    }

    @Override
    @Transactional
    public void deleteJob(UUID jobId) {
        distributionJobRepository.deleteById(jobId);
    }

    private List<DistributionJobModel> getMatchingEnabledJobs(Supplier<List<DistributionJobEntity>> getJobs) {
        // TODO change this to return a page of jobs
        List<DistributionJobEntity> matchingEnabledJob = getJobs.get();
        return matchingEnabledJob
                   .stream()
                   .map(this::convertToDistributionJobModel)
                   .collect(Collectors.toList());
    }

    private DistributionJobModel createJobWithId(UUID jobId, DistributionJobRequestModel requestModel, OffsetDateTime createdAt, @Nullable OffsetDateTime lastUpdated) {
        String channelDescriptorName = requestModel.getChannelDescriptorName();
        DistributionJobEntity jobToSave = new DistributionJobEntity(
            jobId,
            requestModel.getName(),
            requestModel.isEnabled(),
            requestModel.getDistributionFrequency().name(),
            requestModel.getProcessingType().name(),
            channelDescriptorName,
            createdAt,
            lastUpdated
        );
        DistributionJobEntity savedJobEntity = distributionJobRepository.save(jobToSave);
        UUID savedJobId = savedJobEntity.getJobId();

        BlackDuckJobDetailsEntity savedBlackDuckJobDetails = blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(savedJobId, requestModel);
        savedJobEntity.setBlackDuckJobDetails(savedBlackDuckJobDetails);

        DistributionJobDetailsModel distributionJobDetails = requestModel.getDistributionJobDetails();
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

        return convertToDistributionJobModel(savedJobEntity);
    }

    private DistributionJobModel convertToDistributionJobModel(DistributionJobEntity jobEntity) {
        UUID jobId = jobEntity.getJobId();
        DistributionJobDetailsModel distributionJobDetailsModel = null;
        String channelDescriptorName = jobEntity.getChannelDescriptorName();
        if ("channel_azure_boards".equals(channelDescriptorName)) {
            AzureBoardsJobDetailsEntity jobDetails = jobEntity.getAzureBoardsJobDetails();
            distributionJobDetailsModel = new AzureBoardsJobDetailsModel(
                jobDetails.getAddComments(),
                jobDetails.getProjectNameOrId(),
                jobDetails.getWorkItemType(),
                jobDetails.getWorkItemCompletedState(),
                jobDetails.getWorkItemReopenState()
            );
        } else if ("channel_email".equals(channelDescriptorName)) {
            EmailJobDetailsEntity jobDetails = jobEntity.getEmailJobDetails();
            distributionJobDetailsModel = new EmailJobDetailsModel(
                jobDetails.getSubjectLine(),
                jobDetails.getProjectOwnerOnly(),
                jobDetails.getAdditionalEmailAddressesOnly(),
                jobDetails.getAttachmentFileType(),
                jobDetails.getEmailJobAdditionalEmailAddresses().stream()
                    .map(EmailJobAdditionalEmailAddressEntity::getEmailAddress)
                    .collect(Collectors.toList())
            );
        } else if ("channel_jira_cloud".equals(channelDescriptorName)) {
            JiraCloudJobDetailsEntity jobDetails = jobEntity.getJiraCloudJobDetails();
            distributionJobDetailsModel = new JiraCloudJobDetailsModel(
                jobDetails.getAddComments(),
                jobDetails.getIssueCreatorEmail(),
                jobDetails.getProjectNameOrKey(),
                jobDetails.getIssueType(),
                jobDetails.getResolveTransition(),
                jobDetails.getReopenTransition()
            );
        } else if ("channel_jira_server".equals(channelDescriptorName)) {
            JiraServerJobDetailsEntity jobDetails = jobEntity.getJiraServerJobDetails();
            distributionJobDetailsModel = new JiraServerJobDetailsModel(
                jobDetails.getAddComments(),
                jobDetails.getIssueCreatorUsername(),
                jobDetails.getProjectNameOrKey(),
                jobDetails.getIssueType(),
                jobDetails.getResolveTransition(),
                jobDetails.getReopenTransition()
            );
        } else if ("msteamskey".equals(channelDescriptorName)) {
            MSTeamsJobDetailsEntity jobDetails = jobEntity.getMsTeamsJobDetails();
            distributionJobDetailsModel = new MSTeamsJobDetailsModel(jobDetails.getWebhook());
        } else if ("channel_slack".equals(channelDescriptorName)) {
            SlackJobDetailsEntity slackJobDetails = jobEntity.getSlackJobDetails();
            distributionJobDetailsModel = new SlackJobDetailsModel(
                slackJobDetails.getWebhook(),
                slackJobDetails.getChannelName(),
                slackJobDetails.getChannelUsername()
            );
        }

        BlackDuckJobDetailsEntity blackDuckJobDetails = jobEntity.getBlackDuckJobDetails();
        List<String> notificationTypes = blackDuckJobDetailsAccessor.retrieveNotificationTypesForJob(jobId);
        List<BlackDuckProjectDetailsModel> projectDetails = blackDuckJobDetailsAccessor.retrieveProjectDetailsForJob(jobId);
        List<String> policyNames = blackDuckJobDetailsAccessor.retrievePolicyNamesForJob(jobId);
        List<String> vulnerabilitySeverityNames = blackDuckJobDetailsAccessor.retrieveVulnerabilitySeverityNamesForJob(jobId);

        return new DistributionJobModelBuilder()
                   .jobId(jobId)
                   .name(jobEntity.getName())
                   .enabled(jobEntity.getEnabled())
                   .distributionFrequency(jobEntity.getDistributionFrequency())
                   .processingType(jobEntity.getProcessingType())
                   .channelDescriptorName(channelDescriptorName)
                   .createdAt(jobEntity.getCreatedAt())
                   .lastUpdated(jobEntity.getLastUpdated())
                   .blackDuckGlobalConfigId(blackDuckJobDetails.getGlobalConfigId())
                   .filterByProject(blackDuckJobDetails.getFilterByProject())
                   .projectNamePattern(blackDuckJobDetails.getProjectNamePattern())
                   .notificationTypes(notificationTypes)
                   .projectFilterDetails(projectDetails)
                   .policyFilterPolicyNames(policyNames)
                   .vulnerabilityFilterSeverityNames(vulnerabilitySeverityNames)
                   .distributionJobDetails(distributionJobDetailsModel)
                   .build();
    }

}
