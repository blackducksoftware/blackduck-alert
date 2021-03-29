/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModelV2;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.job.DistributionJobEntity;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.azure.boards.DefaultAzureBoardsJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;
import com.synopsys.integration.alert.database.job.email.DefaultEmailJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.email.EmailJobDetailsEntity;
import com.synopsys.integration.alert.database.job.email.additional.EmailJobAdditionalEmailAddressEntity;
import com.synopsys.integration.alert.database.job.jira.cloud.DefaultJiraCloudJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.server.DefaultJiraServerJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsEntity;
import com.synopsys.integration.alert.database.job.msteams.DefaultMSTeamsJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.msteams.MSTeamsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.slack.DefaultSlackJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.slack.SlackJobDetailsEntity;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class StaticJobAccessor implements JobAccessor {
    private final DistributionJobRepository distributionJobRepository;
    private final BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor;
    private final DefaultAzureBoardsJobDetailsAccessor azureBoardsJobDetailsAccessor;
    private final DefaultEmailJobDetailsAccessor emailJobDetailsAccessor;
    private final DefaultJiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor;
    private final DefaultJiraServerJobDetailsAccessor jiraServerJobDetailsAccessor;
    private final DefaultMSTeamsJobDetailsAccessor msTeamsJobDetailsAccessor;
    private final DefaultSlackJobDetailsAccessor slackJobDetailsAccessor;

    // Temporary until all three tiers of the application have been updated to new Job models
    // BlackDuck is currently the only provider, so this is safe in the short-term while we transition to new models
    private final ProviderKey blackDuckProviderKey;

    @Autowired
    public StaticJobAccessor(
        DistributionJobRepository distributionJobRepository,
        BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor,
        DefaultAzureBoardsJobDetailsAccessor azureBoardsJobDetailsAccessor,
        DefaultEmailJobDetailsAccessor emailJobDetailsAccessor,
        DefaultJiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor,
        DefaultJiraServerJobDetailsAccessor jiraServerJobDetailsAccessor,
        DefaultMSTeamsJobDetailsAccessor msTeamsJobDetailsAccessor,
        DefaultSlackJobDetailsAccessor slackJobDetailsAccessor,
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

    //TODO: Is this even used anymore? This is just used in tests
    @Override
    @Transactional(readOnly = true)
    public List<DistributionJobModel> getMatchingEnabledJobs(FrequencyType frequency, Long providerConfigId, NotificationType notificationType) {
        // TODO change this to return a page of jobs
        return getMatchingEnabledJobs(() -> distributionJobRepository.findMatchingEnabledJob(frequency.name(), providerConfigId, notificationType.name()));
    }

    //TODO: Is this even used anymore? This is just used in tests
    @Override
    @Transactional(readOnly = true)
    public List<DistributionJobModel> getMatchingEnabledJobs(Long providerConfigId, NotificationType notificationType) {
        // TODO change this to return a page of jobs
        return getMatchingEnabledJobs(() -> distributionJobRepository.findMatchingEnabledJob(providerConfigId, notificationType.name()));
    }

    @Override
    public AlertPagedModel<FilteredDistributionJobResponseModel> getMatchingEnabledJobs(FilteredDistributionJobRequestModel filteredDistributionJobRequestModel, int pageNumber, int pageLimit) {
        List<String> frequencyTypes = filteredDistributionJobRequestModel.getFrequencyTypes()
                                          .stream()
                                          .map(Enum::name)
                                          .collect(Collectors.toList());

        NotificationType notificationType = filteredDistributionJobRequestModel.getNotificationType();
        String projectName = filteredDistributionJobRequestModel.getProjectName();

        List<String> policyNames = filteredDistributionJobRequestModel.getPolicyNames();
        List<String> vulnerabilitySeverities = filteredDistributionJobRequestModel.getVulnerabilitySeverities();

        List<DistributionJobEntity> distributionJobEntities;
        /*
        if (filteredDistributionJobRequestModel.isPolicyNotification()) {
            distributionJobEntities = distributionJobRepository.findMatchingEnabledJobsWithPolicyNames(frequencyTypes, notificationType.name(), projectName, policyNames);
        } else if (filteredDistributionJobRequestModel.isVulnerabilityNotification()) {
            distributionJobEntities = distributionJobRepository.findMatchingEnabledJobsWithVulnerabilitySeverities(frequencyTypes, notificationType.name(), projectName, vulnerabilitySeverities);
        } else {
            distributionJobEntities = distributionJobRepository.findMatchingEnabledJobs(frequencyTypes, notificationType.name(), projectName);
        }
         */
        PageRequest pageRequest = PageRequest.of(pageNumber, pageLimit);
        Page<DistributionJobEntity> pageOfDistributionJobEntities;
        if (filteredDistributionJobRequestModel.isPolicyNotification()) {
            pageOfDistributionJobEntities = distributionJobRepository.findMatchingEnabledJobsWithPolicyNames(frequencyTypes, notificationType.name(), projectName, policyNames, pageRequest);
        } else if (filteredDistributionJobRequestModel.isVulnerabilityNotification()) {
            pageOfDistributionJobEntities = distributionJobRepository.findMatchingEnabledJobsWithVulnerabilitySeverities(frequencyTypes, notificationType.name(), projectName, vulnerabilitySeverities, pageRequest);
        } else {
            pageOfDistributionJobEntities = distributionJobRepository.findMatchingEnabledJobs(frequencyTypes, notificationType.name(), projectName, pageRequest);
        }
        distributionJobEntities = pageOfDistributionJobEntities.getContent();

        // TODO running project name pattern checks in java code, try to do this in SQL instead (Won't need to return DistributionJobEntity anymore if this happens
        List<FilteredDistributionJobResponseModel> distributionJobResponseModels = distributionJobEntities.stream()
                                                                                       .filter(distributionJobEntity -> filterByProjects(distributionJobEntity, projectName))
                                                                                       .map(this::convertToFilteredDistributionJobResponseModel)
                                                                                       .collect(Collectors.toList());
        return new AlertPagedModel<>(pageOfDistributionJobEntities.getTotalPages(), pageNumber, pageLimit, distributionJobResponseModels);
    }

    @Override
    public AlertPagedModel<FilteredDistributionJobResponseModel> getMatchingEnabledJobsByFilteredNotifications(FilteredDistributionJobRequestModelV2 filteredDistributionJobRequestModel, int pageNumber, int pageLimit) {
        List<String> frequencyTypes = filteredDistributionJobRequestModel.getFrequencyTypes()
                                          .stream()
                                          .map(Enum::name)
                                          .collect(Collectors.toList());

        Set<String> projectNames = filteredDistributionJobRequestModel.getProjectName();
        Set<String> notificationTypes = filteredDistributionJobRequestModel.getProjectName();
        Set<String> policyNames = filteredDistributionJobRequestModel.getPolicyNames();
        Set<String> vulnerabilitySeverities = filteredDistributionJobRequestModel.getVulnerabilitySeverities();

        PageRequest pageRequest = PageRequest.of(pageNumber, pageLimit);
        Page<DistributionJobEntity> pageOfDistributionJobEntities;
        pageOfDistributionJobEntities = distributionJobRepository.findMatchingEnabledJobsByFilteredNotifications(frequencyTypes, notificationTypes, projectNames, policyNames, vulnerabilitySeverities, pageRequest);

        List<DistributionJobEntity> distributionJobEntities = pageOfDistributionJobEntities.getContent();

        List<FilteredDistributionJobResponseModel> distributionJobResponseModels = distributionJobEntities.stream()
                                                                                       .filter(distributionJobEntity -> filterByProjects(distributionJobEntity, projectNames))
                                                                                       .map(this::convertToFilteredDistributionJobResponseModel)
                                                                                       .collect(Collectors.toList());
        return new AlertPagedModel<>(pageOfDistributionJobEntities.getTotalPages(), pageNumber, pageLimit, distributionJobResponseModels);
    }

    //TODO: Simplify this since its a near copy of the above method.
    //TODO: Also, add this method to the JobAccessor interface
    @Override
    public AlertPagedModel<FilteredDistributionJobResponseModel> getMatchingEnabledJobsByJobIds(FilteredDistributionJobRequestModel filteredDistributionJobRequestModel, List<UUID> jobIds, int pageNumber, int pageLimit) {
        List<String> frequencyTypes = filteredDistributionJobRequestModel.getFrequencyTypes()
                                          .stream()
                                          .map(Enum::name)
                                          .collect(Collectors.toList());

        NotificationType notificationType = filteredDistributionJobRequestModel.getNotificationType();
        String projectName = filteredDistributionJobRequestModel.getProjectName();

        List<String> policyNames = filteredDistributionJobRequestModel.getPolicyNames();
        List<String> vulnerabilitySeverities = filteredDistributionJobRequestModel.getVulnerabilitySeverities();

        List<DistributionJobEntity> distributionJobEntities;

        PageRequest pageRequest = PageRequest.of(pageNumber, pageLimit);
        Page<DistributionJobEntity> pageOfDistributionJobEntities;
        //TODO: This is the only part thats different!
        if (filteredDistributionJobRequestModel.isPolicyNotification()) {
            pageOfDistributionJobEntities = distributionJobRepository.findMatchingEnabledJobsWithPolicyNamesByJobId(frequencyTypes, notificationType.name(), projectName, policyNames, jobIds, pageRequest);
        } else if (filteredDistributionJobRequestModel.isVulnerabilityNotification()) {
            pageOfDistributionJobEntities = distributionJobRepository.findMatchingEnabledJobsWithVulnerabilitySeveritiesByJobId(frequencyTypes, notificationType.name(), projectName, vulnerabilitySeverities, jobIds, pageRequest);
        } else {
            pageOfDistributionJobEntities = distributionJobRepository.findMatchingEnabledJobsByJobId(frequencyTypes, notificationType.name(), projectName, jobIds, pageRequest);
        }
        distributionJobEntities = pageOfDistributionJobEntities.getContent();

        // TODO running project name pattern checks in java code, try to do this in SQL instead (Won't need to return DistributionJobEntity anymore if this happens
        List<FilteredDistributionJobResponseModel> distributionJobResponseModels = distributionJobEntities.stream()
                                                                                       .filter(distributionJobEntity -> filterByProjects(distributionJobEntity, projectName))
                                                                                       .map(this::convertToFilteredDistributionJobResponseModel)
                                                                                       .collect(Collectors.toList());
        return new AlertPagedModel<>(pageOfDistributionJobEntities.getTotalPages(), pageNumber, pageLimit, distributionJobResponseModels);
    }

    private boolean filterByProjects(DistributionJobEntity distributionJobEntity, String projectName) {
        BlackDuckJobDetailsEntity blackDuckJobDetails = distributionJobEntity.getBlackDuckJobDetails();
        if (!blackDuckJobDetails.getFilterByProject()) {
            return true;
        }

        String projectNamePattern = blackDuckJobDetails.getProjectNamePattern();
        if (projectNamePattern != null && Pattern.matches(projectNamePattern, projectName)) {
            return true;
        }

        return blackDuckJobDetailsAccessor.retrieveProjectDetailsForJob(distributionJobEntity.getJobId())
                   .stream()
                   .map(BlackDuckProjectDetailsModel::getName)
                   .anyMatch(projectName::equals);
    }

    //TODO: See if we can refactor this and the above method
    private boolean filterByProjects(DistributionJobEntity distributionJobEntity, Set<String> projectNames) {
        BlackDuckJobDetailsEntity blackDuckJobDetails = distributionJobEntity.getBlackDuckJobDetails();
        if (!blackDuckJobDetails.getFilterByProject()) {
            return true;
        }

        String projectNamePattern = blackDuckJobDetails.getProjectNamePattern();
        for (String projectName : projectNames) {
            if (projectNamePattern != null && Pattern.matches(projectNamePattern, projectName)) {
                return true;
            }
        }

        return blackDuckJobDetailsAccessor.retrieveProjectDetailsForJob(distributionJobEntity.getJobId())
                   .stream()
                   .map(BlackDuckProjectDetailsModel::getName)
                   .anyMatch(projectNames::contains);
    }

    private FilteredDistributionJobResponseModel convertToFilteredDistributionJobResponseModel(DistributionJobEntity distributionJobEntity) {
        ProcessingType processingType = Enum.valueOf(ProcessingType.class, distributionJobEntity.getProcessingType());
        return new FilteredDistributionJobResponseModel(distributionJobEntity.getJobId(), processingType, distributionJobEntity.getChannelDescriptorName());
    }

    //TODO: If the top 2 methods for getMatchingEnabledJobs are removed, this one should be too.
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
    public DistributionJobModel createJob(DistributionJobRequestModel requestModel) {
        return createJobWithId(null, requestModel, DateUtils.createCurrentDateTimestamp(), null);
    }

    @Override
    @Transactional
    public DistributionJobModel updateJob(UUID jobId, DistributionJobRequestModel requestModel) throws AlertConfigurationException {
        DistributionJobEntity jobEntity = distributionJobRepository.findById(jobId)
                                              .orElseThrow(() -> new AlertConfigurationException(String.format("No job exists with the id [%s]", jobId.toString())));
        OffsetDateTime createdAt = jobEntity.getCreatedAt();

        deleteJob(jobId);
        return createJobWithId(jobId, requestModel, createdAt, DateUtils.createCurrentDateTimestamp());
    }

    @Override
    @Transactional
    public void deleteJob(UUID jobId) {
        distributionJobRepository.deleteById(jobId);
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
        } else if (distributionJobDetails.isA(ChannelKeys.EMAIL)) {
            EmailJobDetailsEntity savedEmailJobDetails = emailJobDetailsAccessor.saveEmailJobDetails(savedJobId, distributionJobDetails.getAs(DistributionJobDetailsModel.EMAIL));
            savedJobEntity.setEmailJobDetails(savedEmailJobDetails);
        } else if (distributionJobDetails.isA(ChannelKeys.JIRA_CLOUD)) {
            JiraCloudJobDetailsEntity savedJiraCloudJobDetails = jiraCloudJobDetailsAccessor.saveJiraCloudJobDetails(savedJobId, distributionJobDetails.getAs(DistributionJobDetailsModel.JIRA_CLOUD));
            savedJobEntity.setJiraCloudJobDetails(savedJiraCloudJobDetails);
        } else if (distributionJobDetails.isA(ChannelKeys.JIRA_SERVER)) {
            JiraServerJobDetailsEntity savedJiraServerJobDetails = jiraServerJobDetailsAccessor.saveJiraServerJobDetails(savedJobId, distributionJobDetails.getAs(DistributionJobDetailsModel.JIRA_SERVER));
            savedJobEntity.setJiraServerJobDetails(savedJiraServerJobDetails);
        } else if (distributionJobDetails.isA(ChannelKeys.MS_TEAMS)) {
            MSTeamsJobDetailsEntity savedMSTeamsJobDetails = msTeamsJobDetailsAccessor.saveMSTeamsJobDetails(savedJobId, distributionJobDetails.getAs(DistributionJobDetailsModel.MS_TEAMS));
            savedJobEntity.setMsTeamsJobDetails(savedMSTeamsJobDetails);
        } else if (distributionJobDetails.isA(ChannelKeys.SLACK)) {
            SlackJobDetailsEntity savedSlackJobDetails = slackJobDetailsAccessor.saveSlackJobDetails(savedJobId, distributionJobDetails.getAs(DistributionJobDetailsModel.SLACK));
            savedJobEntity.setSlackJobDetails(savedSlackJobDetails);
        }

        return convertToDistributionJobModel(savedJobEntity);
    }

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
        } else if (ChannelKeys.EMAIL.equals(channelKey)) {
            EmailJobDetailsEntity jobDetails = jobEntity.getEmailJobDetails();
            List<String> additionalEmailAddresses = jobDetails.getEmailJobAdditionalEmailAddresses()
                                                        .stream()
                                                        .map(EmailJobAdditionalEmailAddressEntity::getEmailAddress)
                                                        .collect(Collectors.toList());
            distributionJobDetailsModel = new EmailJobDetailsModel(
                jobId,
                jobDetails.getSubjectLine(),
                jobDetails.getProjectOwnerOnly(),
                jobDetails.getAdditionalEmailAddressesOnly(),
                jobDetails.getAttachmentFileType(),
                additionalEmailAddresses
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
                customFields
            );
        } else if (ChannelKeys.JIRA_SERVER.equals(channelKey)) {
            JiraServerJobDetailsEntity jobDetails = jobEntity.getJiraServerJobDetails();
            List<JiraJobCustomFieldModel> customFields = jobDetails.getJobCustomFields()
                                                             .stream()
                                                             .map(entity -> new JiraJobCustomFieldModel(entity.getFieldName(), entity.getFieldValue()))
                                                             .collect(Collectors.toList());
            distributionJobDetailsModel = new JiraServerJobDetailsModel(
                jobId,
                jobDetails.getAddComments(),
                jobDetails.getIssueCreatorUsername(),
                jobDetails.getProjectNameOrKey(),
                jobDetails.getIssueType(),
                jobDetails.getResolveTransition(),
                jobDetails.getReopenTransition(),
                customFields
            );
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
                   .notificationTypes(notificationTypes)
                   .projectFilterDetails(projectDetails)
                   .policyFilterPolicyNames(policyNames)
                   .vulnerabilityFilterSeverityNames(vulnerabilitySeverityNames)
                   .distributionJobDetails(distributionJobDetailsModel)
                   .createdAt(jobEntity.getCreatedAt())
                   .lastUpdated(jobEntity.getLastUpdated())
                   .build();
    }

}
