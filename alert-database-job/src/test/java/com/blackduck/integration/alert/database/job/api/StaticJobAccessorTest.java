/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.descriptor.model.ProviderKey;
import com.blackduck.integration.alert.channel.email.database.accessor.DefaultEmailJobDetailsAccessor;
import com.blackduck.integration.alert.channel.email.database.job.EmailJobDetailsEntity;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.DefaultJiraServerJobDetailsAccessor;
import com.blackduck.integration.alert.channel.jira.server.database.job.JiraServerJobDetailsEntity;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.job.DistributionJobEntity;
import com.blackduck.integration.alert.database.job.DistributionJobRepository;
import com.blackduck.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsEntity;
import com.blackduck.integration.alert.database.job.azure.boards.DefaultAzureBoardsJobDetailsAccessor;
import com.blackduck.integration.alert.database.job.blackduck.BlackDuckJobDetailsAccessor;
import com.blackduck.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;
import com.blackduck.integration.alert.database.job.jira.cloud.DefaultJiraCloudJobDetailsAccessor;
import com.blackduck.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsEntity;
import com.blackduck.integration.alert.database.job.msteams.DefaultMSTeamsJobDetailsAccessor;
import com.blackduck.integration.alert.database.job.msteams.MSTeamsJobDetailsEntity;
import com.blackduck.integration.alert.database.job.slack.DefaultSlackJobDetailsAccessor;
import com.blackduck.integration.alert.database.job.slack.SlackJobDetailsEntity;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

class StaticJobAccessorTest {
    private DistributionJobRepository distributionJobRepository;
    private BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor;
    private DefaultSlackJobDetailsAccessor slackJobDetailsAccessor;
    private DefaultAzureBoardsJobDetailsAccessor azureBoardsJobDetailsAccessor;
    private DefaultEmailJobDetailsAccessor emailJobDetailsAccessor;
    private DefaultJiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor;
    private DefaultJiraServerJobDetailsAccessor jiraServerJobDetailsAccessor;
    private DefaultMSTeamsJobDetailsAccessor msTeamsJobDetailsAccessor;
    private JobAccessor jobAccessor;

    private final String jobName = "jobName";

    @BeforeEach
    public void init() {
        distributionJobRepository = Mockito.mock(DistributionJobRepository.class);
        blackDuckJobDetailsAccessor = Mockito.mock(BlackDuckJobDetailsAccessor.class);
        slackJobDetailsAccessor = Mockito.mock(DefaultSlackJobDetailsAccessor.class);
        azureBoardsJobDetailsAccessor = Mockito.mock(DefaultAzureBoardsJobDetailsAccessor.class);
        emailJobDetailsAccessor = Mockito.mock(DefaultEmailJobDetailsAccessor.class);
        jiraCloudJobDetailsAccessor = Mockito.mock(DefaultJiraCloudJobDetailsAccessor.class);
        jiraServerJobDetailsAccessor = Mockito.mock(DefaultJiraServerJobDetailsAccessor.class);
        msTeamsJobDetailsAccessor = Mockito.mock(DefaultMSTeamsJobDetailsAccessor.class);

        Mockito.when(blackDuckJobDetailsAccessor.retrieveNotificationTypesForJob(Mockito.any())).thenReturn(List.of(NotificationType.LICENSE_LIMIT.name()));
        Mockito.when(blackDuckJobDetailsAccessor.retrieveProjectDetailsForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(blackDuckJobDetailsAccessor.retrievePolicyNamesForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(blackDuckJobDetailsAccessor.retrieveVulnerabilitySeverityNamesForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(emailJobDetailsAccessor.getDescriptorKey()).thenReturn(ChannelKeys.EMAIL);
        Mockito.when(jiraServerJobDetailsAccessor.getDescriptorKey()).thenReturn(ChannelKeys.JIRA_SERVER);

        jobAccessor = new StaticJobAccessor(
            distributionJobRepository,
            blackDuckJobDetailsAccessor,
            azureBoardsJobDetailsAccessor,
            jiraCloudJobDetailsAccessor,
            msTeamsJobDetailsAccessor,
            slackJobDetailsAccessor,
            new BlackDuckProviderKey(),
            List.of(emailJobDetailsAccessor)
        );
    }

    @Test
    void countJobsByFrequencyTest() {
        Mockito.when(distributionJobRepository.existsByDistributionFrequency(FrequencyType.DAILY.name())).thenReturn(true);
        assertTrue(jobAccessor.hasJobsByFrequency(FrequencyType.DAILY.name()));

    }

    @Test
    void getJobByIdTest() {
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*", "*"));

        Mockito.when(distributionJobRepository.findById(jobId)).thenReturn(Optional.of(distributionJobEntity));
        Optional<DistributionJobModel> jobById = jobAccessor.getJobById(jobId);

        assertTrue(jobById.isPresent());
        DistributionJobModel distributionJobModel = jobById.get();
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getJobByNameTest() {
        UUID jobId = UUID.randomUUID();
        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*", "*"));

        Mockito.when(distributionJobRepository.findByName(Mockito.any())).thenReturn(Optional.of(distributionJobEntity));
        Optional<DistributionJobModel> jobByName = jobAccessor.getJobByName(jobName);

        assertTrue(jobByName.isPresent());
        DistributionJobModel distributionJobModel = jobByName.get();
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void createJobTest() {
        UUID jobId = UUID.randomUUID();
        SlackJobDetailsModel slackJobDetailsModel = new SlackJobDetailsModel(jobId, null, null);
        DistributionJobRequestModel distributionJobRequestModel = createDistributionJobEntity(ChannelKeys.SLACK.getUniversalKey(), slackJobDetailsModel);

        SlackJobDetailsEntity slackJobDetailsEntity = new SlackJobDetailsEntity();
        DistributionJobEntity distributionJobEntity = createDistributionJobEntity(jobId, distributionJobRequestModel);
        distributionJobEntity.setSlackJobDetails(slackJobDetailsEntity);
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = createBlackDuckJobDetailsEntity(jobId, distributionJobRequestModel);

        Mockito.when(slackJobDetailsAccessor.saveSlackJobDetails(Mockito.any(), Mockito.any())).thenReturn(slackJobDetailsEntity);
        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel createdJob = jobAccessor.createJob(distributionJobRequestModel);

        assertEquals(jobId, createdJob.getJobId());
        assertEquals(jobName, createdJob.getName());
    }

    @Test
    void deleteJobTest() {
        UUID jobId = UUID.randomUUID();
        jobAccessor.deleteJob(jobId);
        Mockito.verify(distributionJobRepository).deleteById(Mockito.any());
    }

    @Test
    void updateJobTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        SlackJobDetailsModel slackJobDetailsModel = new SlackJobDetailsModel(jobId, null, null);
        DistributionJobRequestModel distributionJobRequestModel = new DistributionJobRequestModel(
            true,
            jobName,
            FrequencyType.DAILY,
            ProcessingType.DEFAULT,
            ChannelKeys.SLACK.getUniversalKey(),
            UUID.randomUUID(),
            3L,
            true,
            "*",
            "*",
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            slackJobDetailsModel
        );

        SlackJobDetailsEntity slackJobDetailsEntity = new SlackJobDetailsEntity();
        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(
            jobId,
            distributionJobRequestModel.getName(),
            distributionJobRequestModel.isEnabled(),
            distributionJobRequestModel.getDistributionFrequency().name(),
            distributionJobRequestModel.getProcessingType().name(),
            distributionJobRequestModel.getChannelDescriptorName(),
            distributionJobRequestModel.getChannelGlobalConfigId(),
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp()
        );
        distributionJobEntity.setSlackJobDetails(slackJobDetailsEntity);
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = new BlackDuckJobDetailsEntity(
            jobId,
            distributionJobRequestModel.getBlackDuckGlobalConfigId(),
            distributionJobRequestModel.isFilterByProject(),
            distributionJobRequestModel.getProjectNamePattern().orElse(null),
            distributionJobRequestModel.getProjectVersionNamePattern().orElse(null)
        );

        Mockito.when(slackJobDetailsAccessor.saveSlackJobDetails(Mockito.any(), Mockito.any())).thenReturn(slackJobDetailsEntity);
        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.findById(Mockito.any())).thenReturn(Optional.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel updatedJob = jobAccessor.updateJob(jobId, distributionJobRequestModel);

        Mockito.verify(distributionJobRepository).findById(jobId);
        Mockito.verify(distributionJobRepository).save(Mockito.any());

        assertEquals(jobId, updatedJob.getJobId());
        assertEquals(jobName, updatedJob.getName());
    }

    @Test
    void getJobsByIdTest() {
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*", "*"));

        Mockito.when(distributionJobRepository.findAllById(Mockito.any())).thenReturn(List.of(distributionJobEntity));

        List<DistributionJobModel> jobsById = jobAccessor.getJobsById(List.of(jobId));

        assertEquals(1, jobsById.size());
        DistributionJobModel distributionJobModel = jobsById.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getPageOfJobsTest() {
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*", "*"));

        Page<DistributionJobEntity> page = new PageImpl<>(List.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(page);

        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10);

        assertEquals(1, pageOfJobs.getTotalPages());
        List<DistributionJobModel> models = pageOfJobs.getModels();
        assertEquals(1, models.size());
        DistributionJobModel distributionJobModel = models.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getPageOfJobsEmailTest() {
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = createEmailDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*", "*"));
        Page<DistributionJobEntity> page = new PageImpl<>(List.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(page);
        Mockito.when(emailJobDetailsAccessor.retrieveDetails(Mockito.any())).thenReturn(Optional.of(new EmailJobDetailsModel(
            jobId,
            null,
            false,
            false,
            "NONE",
            List.of()
        )));

        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10);

        assertEquals(1, pageOfJobs.getTotalPages());
        List<DistributionJobModel> models = pageOfJobs.getModels();
        assertEquals(1, models.size());
        DistributionJobModel distributionJobModel = models.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getPageOfJobsSearchTest() {
        ProviderKey providerKey = new BlackDuckProviderKey();
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*", "*"));

        Page<DistributionJobEntity> page = new PageImpl<>(List.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.findByChannelDescriptorNamesAndSearchTerm(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(page);

        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10, "test-search-term", null, null, List.of(providerKey.getUniversalKey()));

        assertEquals(1, pageOfJobs.getTotalPages());
        List<DistributionJobModel> models = pageOfJobs.getModels();
        assertEquals(1, models.size());
        DistributionJobModel distributionJobModel = models.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getPageOfJobsSearchAndDefaultSortTest() {
        ProviderKey providerKey = new BlackDuckProviderKey();
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*", "*"));

        Page<DistributionJobEntity> page = new PageImpl<>(List.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.findByChannelDescriptorNamesAndSearchTerm(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(page);

        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10, "test-search-term", "name", "asc", List.of(providerKey.getUniversalKey()));

        assertEquals(1, pageOfJobs.getTotalPages());
        List<DistributionJobModel> models = pageOfJobs.getModels();
        assertEquals(1, models.size());
        DistributionJobModel distributionJobModel = models.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getPageOfJobsBlankSearchTest() {
        ProviderKey providerKey = new BlackDuckProviderKey();
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*", "*"));

        Page<DistributionJobEntity> page = new PageImpl<>(List.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.findByChannelDescriptorNameIn(Mockito.any(), Mockito.any())).thenReturn(page);

        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10, " ", null, null, List.of(providerKey.getUniversalKey()));

        assertEquals(1, pageOfJobs.getTotalPages());
        List<DistributionJobModel> models = pageOfJobs.getModels();
        assertEquals(1, models.size());
        DistributionJobModel distributionJobModel = models.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getPageOfJobsExcludedDescriptorTest() {
        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10, null, null, null, List.of("invalid-descriptor-key"));

        assertEquals(0, pageOfJobs.getTotalPages());
        assertEquals(0, pageOfJobs.getCurrentPage());
        assertEquals(10, pageOfJobs.getPageSize());
        assertEquals(0, pageOfJobs.getModels().size());
    }

    @Test
    void createAzureBoardsJobTest() {
        UUID jobId = UUID.randomUUID();
        AzureBoardsJobDetailsModel azureBoardsJobDetailsModel = new AzureBoardsJobDetailsModel(jobId, false, null, null, null, null);
        DistributionJobRequestModel distributionJobRequestModel = createDistributionJobEntity(ChannelKeys.AZURE_BOARDS.getUniversalKey(), azureBoardsJobDetailsModel);

        AzureBoardsJobDetailsEntity azureBoardsJobDetailsEntity = new AzureBoardsJobDetailsEntity(null, false, null, null, null, null);
        DistributionJobEntity distributionJobEntity = createDistributionJobEntity(jobId, distributionJobRequestModel);
        distributionJobEntity.setAzureBoardsJobDetails(azureBoardsJobDetailsEntity);
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = createBlackDuckJobDetailsEntity(jobId, distributionJobRequestModel);

        Mockito.when(azureBoardsJobDetailsAccessor.saveAzureBoardsJobDetails(Mockito.any(), Mockito.any())).thenReturn(azureBoardsJobDetailsEntity);
        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel createdJob = jobAccessor.createJob(distributionJobRequestModel);

        assertEquals(jobId, createdJob.getJobId());
        assertEquals(jobName, createdJob.getName());
    }

    @Test
    void createEmailJobTest() {
        UUID jobId = UUID.randomUUID();
        EmailJobDetailsModel emailJobDetailsModel = new EmailJobDetailsModel(jobId, null, false, false, null, List.of());
        DistributionJobRequestModel distributionJobRequestModel = createDistributionJobEntity(ChannelKeys.EMAIL.getUniversalKey(), emailJobDetailsModel);

        EmailJobDetailsEntity emailJobDetailsEntity = new EmailJobDetailsEntity(null, null, false, false, null);
        emailJobDetailsEntity.setEmailJobAdditionalEmailAddresses(List.of());
        DistributionJobEntity distributionJobEntity = createDistributionJobEntity(jobId, distributionJobRequestModel);
        distributionJobEntity.setEmailJobDetails(emailJobDetailsEntity);
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = createBlackDuckJobDetailsEntity(jobId, distributionJobRequestModel);

        Mockito.when(emailJobDetailsAccessor.saveJobDetails(Mockito.any(), Mockito.any())).thenReturn(emailJobDetailsModel);
        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel createdJob = jobAccessor.createJob(distributionJobRequestModel);

        assertEquals(jobId, createdJob.getJobId());
        assertEquals(jobName, createdJob.getName());
    }

    @Test
    void createJiraCloudJobTest() {
        UUID jobId = UUID.randomUUID();
        JiraCloudJobDetailsModel jiraCloudJobDetailsModel = new JiraCloudJobDetailsModel(jobId, null, null, null, null, null, List.of(), null);
        DistributionJobRequestModel distributionJobRequestModel = createDistributionJobEntity(ChannelKeys.JIRA_CLOUD.getUniversalKey(), jiraCloudJobDetailsModel);

        JiraCloudJobDetailsEntity jiraCloudJobDetailsEntity = new JiraCloudJobDetailsEntity(null, null, null, null, null, null, null);
        jiraCloudJobDetailsEntity.setJobCustomFields(List.of());
        DistributionJobEntity distributionJobEntity = createDistributionJobEntity(jobId, distributionJobRequestModel);
        distributionJobEntity.setJiraCloudJobDetails(jiraCloudJobDetailsEntity);
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = createBlackDuckJobDetailsEntity(jobId, distributionJobRequestModel);

        Mockito.when(jiraCloudJobDetailsAccessor.saveJiraCloudJobDetails(Mockito.any(), Mockito.any())).thenReturn(jiraCloudJobDetailsEntity);
        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel createdJob = jobAccessor.createJob(distributionJobRequestModel);

        assertEquals(jobId, createdJob.getJobId());
        assertEquals(jobName, createdJob.getName());
    }

    @Test
    void createJiraServerJobTest() {
        UUID jobId = UUID.randomUUID();
        JiraServerJobDetailsModel jiraServerJobDetailsModel = new JiraServerJobDetailsModel(jobId, null, null, null, null, null, List.of(), "issueSummary");
        DistributionJobRequestModel distributionJobRequestModel = createDistributionJobEntity(ChannelKeys.JIRA_SERVER.getUniversalKey(), jiraServerJobDetailsModel);

        JiraServerJobDetailsEntity jiraServerJobDetailsEntity = new JiraServerJobDetailsEntity(null, null, null, null, null, null, null);
        jiraServerJobDetailsEntity.setJobCustomFields(List.of());
        DistributionJobEntity distributionJobEntity = createDistributionJobEntity(jobId, distributionJobRequestModel);
        distributionJobEntity.setJiraServerJobDetails(jiraServerJobDetailsEntity);
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = createBlackDuckJobDetailsEntity(jobId, distributionJobRequestModel);

        Mockito.when(jiraServerJobDetailsAccessor.saveJobDetails(Mockito.any(), Mockito.any())).thenReturn(jiraServerJobDetailsModel);
        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel createdJob = jobAccessor.createJob(distributionJobRequestModel);

        assertEquals(jobId, createdJob.getJobId());
        assertEquals(jobName, createdJob.getName());
    }

    @Test
    void createMSTeamsJobTest() {
        UUID jobId = UUID.randomUUID();
        MSTeamsJobDetailsModel msTeamsJobDetailsModel = new MSTeamsJobDetailsModel(jobId, null);
        DistributionJobRequestModel distributionJobRequestModel = createDistributionJobEntity(ChannelKeys.MS_TEAMS.getUniversalKey(), msTeamsJobDetailsModel);

        MSTeamsJobDetailsEntity msTeamsJobDetailsEntity = new MSTeamsJobDetailsEntity();
        DistributionJobEntity distributionJobEntity = createDistributionJobEntity(jobId, distributionJobRequestModel);
        distributionJobEntity.setMsTeamsJobDetails(msTeamsJobDetailsEntity);
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = createBlackDuckJobDetailsEntity(jobId, distributionJobRequestModel);

        Mockito.when(msTeamsJobDetailsAccessor.saveMSTeamsJobDetails(Mockito.any(), Mockito.any())).thenReturn(msTeamsJobDetailsEntity);
        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel createdJob = jobAccessor.createJob(distributionJobRequestModel);

        assertEquals(jobId, createdJob.getJobId());
        assertEquals(jobName, createdJob.getName());
    }

    private DistributionJobRequestModel createDistributionJobEntity(String channelDescriptorName, DistributionJobDetailsModel distributionJobDetails) {
        return new DistributionJobRequestModel(
            true,
            jobName,
            FrequencyType.DAILY,
            ProcessingType.DEFAULT,
            channelDescriptorName,
            UUID.randomUUID(),
            3L,
            true,
            "*",
            "*",
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            distributionJobDetails
        );
    }

    private DistributionJobEntity createDistributionJobEntity(UUID jobId, DistributionJobRequestModel distributionJobRequestModel) {
        return new DistributionJobEntity(
            jobId,
            distributionJobRequestModel.getName(),
            distributionJobRequestModel.isEnabled(),
            distributionJobRequestModel.getDistributionFrequency().name(),
            distributionJobRequestModel.getProcessingType().name(),
            distributionJobRequestModel.getChannelDescriptorName(),
            distributionJobRequestModel.getChannelGlobalConfigId(),
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp()
        );
    }

    private BlackDuckJobDetailsEntity createBlackDuckJobDetailsEntity(UUID jobId, DistributionJobRequestModel distributionJobRequestModel) {
        return new BlackDuckJobDetailsEntity(
            jobId,
            distributionJobRequestModel.getBlackDuckGlobalConfigId(),
            distributionJobRequestModel.isFilterByProject(),
            distributionJobRequestModel.getProjectNamePattern().orElse(null),
            distributionJobRequestModel.getProjectVersionNamePattern().orElse(null)
        );
    }

    private DistributionJobEntity createSlackDistributionJobEntity(UUID jobId) {
        SlackJobDetailsEntity slackJobDetailsEntity = new SlackJobDetailsEntity();
        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(
            jobId,
            jobName,
            true,
            FrequencyType.REAL_TIME.name(),
            ProcessingType.DEFAULT.name(),
            ChannelKeys.SLACK.getUniversalKey(),
            UUID.randomUUID(),
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp()
        );
        distributionJobEntity.setSlackJobDetails(slackJobDetailsEntity);
        return distributionJobEntity;
    }

    private DistributionJobEntity createEmailDistributionJobEntity(UUID jobId) {
        EmailJobDetailsEntity emailJobDetailsEntity = new EmailJobDetailsEntity();
        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(
            jobId,
            jobName,
            true,
            FrequencyType.REAL_TIME.name(),
            ProcessingType.DEFAULT.name(),
            ChannelKeys.EMAIL.getUniversalKey(),
            UUID.randomUUID(),
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp()
        );
        distributionJobEntity.setEmailJobDetails(emailJobDetailsEntity);
        return distributionJobEntity;
    }

}
