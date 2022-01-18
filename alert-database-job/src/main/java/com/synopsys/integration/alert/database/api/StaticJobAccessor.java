/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor2;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.job.DistributionJobEntity;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.azure.boards.DefaultAzureBoardsJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.cloud.DefaultJiraCloudJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.server.DefaultJiraServerJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.msteams.DefaultMSTeamsJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.msteams.MSTeamsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.slack.DefaultSlackJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.slack.SlackJobDetailsEntity;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

@Component
public class StaticJobAccessor implements JobAccessor {
    private final DistributionJobRepository distributionJobRepository;
    private final BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor;
    private final DefaultAzureBoardsJobDetailsAccessor azureBoardsJobDetailsAccessor;
    private final DefaultJiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor;
    private final DefaultMSTeamsJobDetailsAccessor msTeamsJobDetailsAccessor;
    private final DefaultSlackJobDetailsAccessor slackJobDetailsAccessor;

    // Temporary until all three tiers of the application have been updated to new Job models
    // BlackDuck is currently the only provider, so this is safe in the short-term while we transition to new models
    private final ProviderKey blackDuckProviderKey;

    private final Map<DescriptorKey, JobDetailsAccessor2<? extends DistributionJobDetailsModel>> jobDetailsAccessorMap;

    @Autowired
    public StaticJobAccessor(
        DistributionJobRepository distributionJobRepository,
        BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor,
        DefaultAzureBoardsJobDetailsAccessor azureBoardsJobDetailsAccessor,
        DefaultJiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor,
        DefaultJiraServerJobDetailsAccessor jiraServerJobDetailsAccessor,
        DefaultMSTeamsJobDetailsAccessor msTeamsJobDetailsAccessor,
        DefaultSlackJobDetailsAccessor slackJobDetailsAccessor,
        ProviderKey blackDuckProviderKey,
        List<JobDetailsAccessor2<? extends DistributionJobDetailsModel>> jobDetailsAccessorList
    ) {
        this.distributionJobRepository = distributionJobRepository;
        this.blackDuckJobDetailsAccessor = blackDuckJobDetailsAccessor;
        this.azureBoardsJobDetailsAccessor = azureBoardsJobDetailsAccessor;
        this.jiraCloudJobDetailsAccessor = jiraCloudJobDetailsAccessor;
        this.msTeamsJobDetailsAccessor = msTeamsJobDetailsAccessor;
        this.slackJobDetailsAccessor = slackJobDetailsAccessor;
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.jobDetailsAccessorMap = DataStructureUtils.mapToValues(jobDetailsAccessorList, JobDetailsAccessor2::getDescriptorKey);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasJobsByFrequency(String frequency) {
        return distributionJobRepository.existsByDistributionFrequency(frequency);
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
        Page<DistributionJobModel> pageOfJobsWithDescriptorNames = distributionJobRepository.findAll(pageRequest).map(this::convertToDistributionJobModelFromEntity);
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

        List<DistributionJobModel> configurationJobModels = pageOfJobsWithDescriptorNames.map(this::convertToDistributionJobModelFromEntity).getContent();
        return new AlertPagedModel<>(pageOfJobsWithDescriptorNames.getTotalPages(), pageNumber, pageLimit, configurationJobModels);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DistributionJobModel> getJobById(UUID jobId) {
        return distributionJobRepository.findById(jobId).map(this::convertToDistributionJobModelFromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DistributionJobModel> getJobByName(String jobName) {
        return distributionJobRepository.findByName(jobName).map(this::convertToDistributionJobModelFromEntity);
    }

    @Override
    @Transactional
    public DistributionJobModel createJob(DistributionJobRequestModel requestModel) {
        return createJobWithId(null, requestModel, DateUtils.createCurrentDateTimestamp(), null);
    }

    @Override
    @Transactional
    public DistributionJobModel updateJob(UUID jobId, DistributionJobRequestModel requestModel) throws AlertConfigurationException {
        DistributionJobEntity jobEntity = distributionJobRepository.findById(jobId)
            .orElseThrow(() -> new AlertConfigurationException(String.format("No job exists with the id [%s]", jobId)));
        OffsetDateTime createdAt = jobEntity.getCreatedAt();

        if (!jobEntity.getChannelDescriptorName().equals(requestModel.getChannelDescriptorName())) {
            // Deleting a Job will affect all tables with foreign keys referencing it.
            // Only delete a job if the channel for which it is configured changes.
            deleteJob(jobId);
        }
        return createJobWithId(jobId, requestModel, createdAt, DateUtils.createCurrentDateTimestamp());
    }

    @Override
    @Transactional
    public void deleteJob(UUID jobId) {
        distributionJobRepository.deleteById(jobId);
    }

    private DistributionJobModel convertToDistributionJobModelFromEntity(DistributionJobEntity distributionJobEntity) {
        DistributionJobModel result;
        ChannelKey channelKey = ChannelKeys.getChannelKey(distributionJobEntity.getChannelDescriptorName());
        if (jobDetailsAccessorMap.containsKey(channelKey)) {
            JobDetailsAccessor2<? extends DistributionJobDetailsModel> accessor = jobDetailsAccessorMap.get(channelKey);
            DistributionJobDetailsModel detailsModel = accessor.retrieveDetails(distributionJobEntity.getJobId()).orElse(null);
            result = convertToDistributionJobModel(distributionJobEntity, detailsModel);
        } else {
            result = convertToDistributionJobModel(distributionJobEntity);
        }
        return result;
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
        if (distributionJobDetails.isA(ChannelKeys.AZURE_BOARDS)) {
            AzureBoardsJobDetailsEntity savedAzureBoardsJobDetails = azureBoardsJobDetailsAccessor.saveAzureBoardsJobDetails(savedJobId, distributionJobDetails.getAs(DistributionJobDetailsModel.AZURE));
            savedJobEntity.setAzureBoardsJobDetails(savedAzureBoardsJobDetails);
        } else if (distributionJobDetails.isA(ChannelKeys.JIRA_CLOUD)) {
            JiraCloudJobDetailsEntity savedJiraCloudJobDetails = jiraCloudJobDetailsAccessor.saveJiraCloudJobDetails(savedJobId, distributionJobDetails.getAs(DistributionJobDetailsModel.JIRA_CLOUD));
            savedJobEntity.setJiraCloudJobDetails(savedJiraCloudJobDetails);
        } else if (distributionJobDetails.isA(ChannelKeys.MS_TEAMS)) {
            MSTeamsJobDetailsEntity savedMSTeamsJobDetails = msTeamsJobDetailsAccessor.saveMSTeamsJobDetails(savedJobId, distributionJobDetails.getAs(DistributionJobDetailsModel.MS_TEAMS));
            savedJobEntity.setMsTeamsJobDetails(savedMSTeamsJobDetails);
        } else if (distributionJobDetails.isA(ChannelKeys.SLACK)) {
            SlackJobDetailsEntity savedSlackJobDetails = slackJobDetailsAccessor.saveSlackJobDetails(savedJobId, distributionJobDetails.getAs(DistributionJobDetailsModel.SLACK));
            savedJobEntity.setSlackJobDetails(savedSlackJobDetails);
        } else {
            // In the future the contents of this else case should be the complete implementation of this method
            ChannelKey channelKey = ChannelKeys.getChannelKey(channelDescriptorName);
            if (jobDetailsAccessorMap.containsKey(channelKey) && distributionJobDetails.isA(channelKey)) {
                JobDetailsAccessor2<? extends DistributionJobDetailsModel> accessor = jobDetailsAccessorMap.get(channelKey);
                DistributionJobDetailsModel savedJobDetails = accessor.saveJobDetails(savedJobId, distributionJobDetails);
                return convertToDistributionJobModel(savedJobEntity, savedJobDetails);
            }
        }

        return convertToDistributionJobModel(savedJobEntity);
    }

    @Deprecated
    private DistributionJobModel convertToDistributionJobModel(DistributionJobEntity jobEntity) {
        UUID jobId = jobEntity.getJobId();
        DistributionJobDetailsModel distributionJobDetailsModel = null;
        ChannelKey channelKey = ChannelKeys.getChannelKey(jobEntity.getChannelDescriptorName());
        if (ChannelKeys.AZURE_BOARDS.equals(channelKey)) {
            AzureBoardsJobDetailsEntity jobDetails = jobEntity.getAzureBoardsJobDetails();
            distributionJobDetailsModel = new AzureBoardsJobDetailsModel(
                jobId,
                jobDetails.getAddComments(),
                jobDetails.getProjectNameOrId(),
                jobDetails.getWorkItemType(),
                jobDetails.getWorkItemCompletedState(),
                jobDetails.getWorkItemReopenState()
            );
        } else if (ChannelKeys.JIRA_CLOUD.equals(channelKey)) {
            JiraCloudJobDetailsEntity jobDetails = jobEntity.getJiraCloudJobDetails();
            List<JiraJobCustomFieldModel> customFields = jobDetails.getJobCustomFields()
                .stream()
                .map(entity -> new JiraJobCustomFieldModel(entity.getFieldName(), entity.getFieldValue()))
                .collect(Collectors.toList());
            distributionJobDetailsModel = new JiraCloudJobDetailsModel(
                jobId,
                jobDetails.getAddComments(),
                jobDetails.getIssueCreatorEmail(),
                jobDetails.getProjectNameOrKey(),
                jobDetails.getIssueType(),
                jobDetails.getResolveTransition(),
                jobDetails.getReopenTransition(),
                customFields,
                jobDetails.getIssueSummary());
        } else if (ChannelKeys.MS_TEAMS.equals(channelKey)) {
            MSTeamsJobDetailsEntity jobDetails = jobEntity.getMsTeamsJobDetails();
            distributionJobDetailsModel = new MSTeamsJobDetailsModel(jobId, jobDetails.getWebhook());
        } else if (ChannelKeys.SLACK.equals(channelKey)) {
            SlackJobDetailsEntity slackJobDetails = jobEntity.getSlackJobDetails();
            distributionJobDetailsModel = new SlackJobDetailsModel(
                jobId,
                slackJobDetails.getWebhook(),
                slackJobDetails.getChannelName(),
                slackJobDetails.getChannelUsername()
            );
        }

        return convertToDistributionJobModel(jobEntity, distributionJobDetailsModel);
    }

    private DistributionJobModel convertToDistributionJobModel(DistributionJobEntity jobEntity, DistributionJobDetailsModel jobDetailsModel) {
        UUID jobId = jobEntity.getJobId();
        ChannelKey channelKey = ChannelKeys.getChannelKey(jobEntity.getChannelDescriptorName());

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
            .channelDescriptorName(channelKey.getUniversalKey())
            .createdAt(jobEntity.getCreatedAt())
            .lastUpdated(jobEntity.getLastUpdated())
            .blackDuckGlobalConfigId(blackDuckJobDetails.getGlobalConfigId())
            .filterByProject(blackDuckJobDetails.getFilterByProject())
            .projectNamePattern(blackDuckJobDetails.getProjectNamePattern())
            .projectVersionNamePattern(blackDuckJobDetails.getProjectVersionNamePattern())
            .notificationTypes(notificationTypes)
            .projectFilterDetails(projectDetails)
            .policyFilterPolicyNames(policyNames)
            .vulnerabilityFilterSeverityNames(vulnerabilitySeverityNames)
            .distributionJobDetails(jobDetailsModel)
            .createdAt(jobEntity.getCreatedAt())
            .lastUpdated(jobEntity.getLastUpdated())
            .build();
    }
}
