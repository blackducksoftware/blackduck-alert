package com.synopsys.integration.alert.database.api;

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

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
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
import com.synopsys.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.azure.boards.DefaultAzureBoardsJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;
import com.synopsys.integration.alert.database.job.email.DefaultEmailJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.email.EmailJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.cloud.DefaultJiraCloudJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsEntity;
import com.synopsys.integration.alert.database.job.msteams.DefaultMSTeamsJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.msteams.MSTeamsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.slack.DefaultSlackJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.slack.SlackJobDetailsEntity;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

class StaticJobAccessorTest {
    private DistributionJobRepository distributionJobRepository;
    private BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor;
    private DefaultSlackJobDetailsAccessor slackJobDetailsAccessor;
    private DefaultAzureBoardsJobDetailsAccessor azureBoardsJobDetailsAccessor;
    private DefaultEmailJobDetailsAccessor emailJobDetailsAccessor;
    private DefaultJiraCloudJobDetailsAccessor jiraCloudJobDetailsAccessor;
    private JiraServerJobDetailsAccessor jiraServerJobDetailsAccessor;
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
        jiraServerJobDetailsAccessor = Mockito.mock(JiraServerJobDetailsAccessor.class);
        msTeamsJobDetailsAccessor = Mockito.mock(DefaultMSTeamsJobDetailsAccessor.class);

        Mockito.when(blackDuckJobDetailsAccessor.retrieveNotificationTypesForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(blackDuckJobDetailsAccessor.retrieveProjectDetailsForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(blackDuckJobDetailsAccessor.retrievePolicyNamesForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(blackDuckJobDetailsAccessor.retrieveVulnerabilitySeverityNamesForJob(Mockito.any())).thenReturn(Collections.emptyList());
        jobAccessor = new StaticJobAccessor(distributionJobRepository,
            blackDuckJobDetailsAccessor,
            azureBoardsJobDetailsAccessor,
            emailJobDetailsAccessor,
            jiraCloudJobDetailsAccessor,
            jiraServerJobDetailsAccessor,
            msTeamsJobDetailsAccessor,
            slackJobDetailsAccessor,
            new BlackDuckProviderKey());
    }

    @Test
    void getJobByIdTest() {
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

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
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

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
        SlackJobDetailsModel slackJobDetailsModel = new SlackJobDetailsModel(jobId, null, null, null);
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
        SlackJobDetailsModel slackJobDetailsModel = new SlackJobDetailsModel(jobId, null, null, null);
        DistributionJobRequestModel distributionJobRequestModel = new DistributionJobRequestModel(
            true,
            jobName,
            FrequencyType.DAILY,
            ProcessingType.DEFAULT,
            ChannelKeys.SLACK.getUniversalKey(),
            3L,
            true,
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
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp()
        );
        distributionJobEntity.setSlackJobDetails(slackJobDetailsEntity);
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = new BlackDuckJobDetailsEntity(
            jobId,
            distributionJobRequestModel.getBlackDuckGlobalConfigId(),
            distributionJobRequestModel.isFilterByProject(),
            distributionJobRequestModel.getProjectNamePattern().orElse(null)
        );

        Mockito.when(slackJobDetailsAccessor.saveSlackJobDetails(Mockito.any(), Mockito.any())).thenReturn(slackJobDetailsEntity);
        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.findById(Mockito.any())).thenReturn(Optional.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel updatedJob = jobAccessor.updateJob(jobId, distributionJobRequestModel);

        Mockito.verify(distributionJobRepository).deleteById(jobId);

        assertEquals(jobId, updatedJob.getJobId());
        assertEquals(jobName, updatedJob.getName());
    }

    @Test
    void getJobsByIdTest() {
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

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
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

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
    void getPageOfJobsSearchTest() {
        ProviderKey providerKey = new BlackDuckProviderKey();
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Page<DistributionJobEntity> page = new PageImpl<>(List.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.findByChannelDescriptorNamesAndSearchTerm(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(page);

        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10, "test-search-term", List.of(providerKey.getUniversalKey()));

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
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Page<DistributionJobEntity> page = new PageImpl<>(List.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.findByChannelDescriptorNameIn(Mockito.any(), Mockito.any())).thenReturn(page);

        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10, " ", List.of(providerKey.getUniversalKey()));

        assertEquals(1, pageOfJobs.getTotalPages());
        List<DistributionJobModel> models = pageOfJobs.getModels();
        assertEquals(1, models.size());
        DistributionJobModel distributionJobModel = models.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getPageOfJobsExcludedDescriptorTest() {
        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10, null, List.of("invalid-descriptor-key"));

        assertEquals(0, pageOfJobs.getTotalPages());
        assertEquals(0, pageOfJobs.getCurrentPage());
        assertEquals(10, pageOfJobs.getPageSize());
        assertEquals(0, pageOfJobs.getModels().size());
    }

    @Test
    void getMatchingEnabledJobsTest() {
        UUID jobId = UUID.randomUUID();
        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Mockito.when(distributionJobRepository.findMatchingEnabledJob(Mockito.any(), Mockito.any())).thenReturn(List.of(distributionJobEntity));

        List<DistributionJobModel> matchingEnabledJobs = jobAccessor.getMatchingEnabledJobs(3L, NotificationType.BOM_EDIT);

        assertEquals(1, matchingEnabledJobs.size());
        DistributionJobModel distributionJobModel = matchingEnabledJobs.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getMatchingEnabledJobsWithFrequencyTest() {
        UUID jobId = UUID.randomUUID();
        DistributionJobEntity distributionJobEntity = createSlackDistributionJobEntity(jobId);
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Mockito.when(distributionJobRepository.findMatchingEnabledJob(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(List.of(distributionJobEntity));

        List<DistributionJobModel> matchingEnabledJobs = jobAccessor.getMatchingEnabledJobs(FrequencyType.DAILY, 3L, NotificationType.BOM_EDIT);

        assertEquals(1, matchingEnabledJobs.size());
        DistributionJobModel distributionJobModel = matchingEnabledJobs.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    public void createAzureBoardsJobTest() {
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
    public void createEmailJobTest() {
        UUID jobId = UUID.randomUUID();
        EmailJobDetailsModel emailJobDetailsModel = new EmailJobDetailsModel(jobId, null, false, false, null, List.of());
        DistributionJobRequestModel distributionJobRequestModel = createDistributionJobEntity(ChannelKeys.EMAIL.getUniversalKey(), emailJobDetailsModel);

        EmailJobDetailsEntity emailJobDetailsEntity = new EmailJobDetailsEntity(null, null, false, false, null);
        emailJobDetailsEntity.setEmailJobAdditionalEmailAddresses(List.of());
        DistributionJobEntity distributionJobEntity = createDistributionJobEntity(jobId, distributionJobRequestModel);
        distributionJobEntity.setEmailJobDetails(emailJobDetailsEntity);
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = createBlackDuckJobDetailsEntity(jobId, distributionJobRequestModel);

        Mockito.when(emailJobDetailsAccessor.saveEmailJobDetails(Mockito.any(), Mockito.any())).thenReturn(emailJobDetailsEntity);
        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel createdJob = jobAccessor.createJob(distributionJobRequestModel);

        assertEquals(jobId, createdJob.getJobId());
        assertEquals(jobName, createdJob.getName());
    }

    @Test
    public void createJiraCloudJobTest() {
        UUID jobId = UUID.randomUUID();
        JiraCloudJobDetailsModel jiraCloudJobDetailsModel = new JiraCloudJobDetailsModel(jobId, false, null, null, null, null, null, List.of());
        DistributionJobRequestModel distributionJobRequestModel = createDistributionJobEntity(ChannelKeys.JIRA_CLOUD.getUniversalKey(), jiraCloudJobDetailsModel);

        JiraCloudJobDetailsEntity jiraCloudJobDetailsEntity = new JiraCloudJobDetailsEntity(null, false, null, null, null, null, null);
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
    public void createJiraServerJobTest() {
        UUID jobId = UUID.randomUUID();
        JiraServerJobDetailsModel jiraServerJobDetailsModel = new JiraServerJobDetailsModel(jobId, false, null, null, null, null, null, List.of());
        DistributionJobRequestModel distributionJobRequestModel = createDistributionJobEntity(ChannelKeys.JIRA_SERVER.getUniversalKey(), jiraServerJobDetailsModel);

        JiraServerJobDetailsEntity jiraServerJobDetailsEntity = new JiraServerJobDetailsEntity(null, false, null, null, null, null, null);
        jiraServerJobDetailsEntity.setJobCustomFields(List.of());
        DistributionJobEntity distributionJobEntity = createDistributionJobEntity(jobId, distributionJobRequestModel);
        distributionJobEntity.setJiraServerJobDetails(jiraServerJobDetailsEntity);
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = createBlackDuckJobDetailsEntity(jobId, distributionJobRequestModel);

        Mockito.when(jiraServerJobDetailsAccessor.saveJiraServerJobDetails(Mockito.any(), Mockito.any())).thenReturn(jiraServerJobDetailsEntity);
        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel createdJob = jobAccessor.createJob(distributionJobRequestModel);

        assertEquals(jobId, createdJob.getJobId());
        assertEquals(jobName, createdJob.getName());
    }

    @Test
    public void createMSTeamsJobTest() {
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
            3L,
            true,
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
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp()
        );
    }

    private BlackDuckJobDetailsEntity createBlackDuckJobDetailsEntity(UUID jobId, DistributionJobRequestModel distributionJobRequestModel) {
        return new BlackDuckJobDetailsEntity(
            jobId,
            distributionJobRequestModel.getBlackDuckGlobalConfigId(),
            distributionJobRequestModel.isFilterByProject(),
            distributionJobRequestModel.getProjectNamePattern().orElse(null)
        );
    }

    private DistributionJobEntity createSlackDistributionJobEntity(UUID jobId) {
        SlackJobDetailsEntity slackJobDetailsEntity = new SlackJobDetailsEntity();
        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(jobId, jobName, true, null, null, ChannelKeys.SLACK.getUniversalKey(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        distributionJobEntity.setSlackJobDetails(slackJobDetailsEntity);
        return distributionJobEntity;
    }

}
