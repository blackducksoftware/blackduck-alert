package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@AlertIntegrationTest
class StaticJobAccessorTestIT {
    private static final List<UUID> createdJobIds = new LinkedList<>();

    @Autowired
    private StaticJobAccessor staticJobAccessor;

    @AfterEach
    private void cleanup() {
        createdJobIds.forEach(staticJobAccessor::deleteJob);
        createdJobIds.clear();
    }

    @Test
    @Transactional
    void verifyAzureSavesTest() {
        AzureBoardsJobDetailsModel azureBoardsJobDetailsModel = new AzureBoardsJobDetailsModel(
            UUID.randomUUID(),
            true,
            "projectNameOrId",
            "workItemType",
            "workItemCompletedState",
            "workItemReopenState"
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(azureBoardsJobDetailsModel, new AzureBoardsChannelKey());
        createAndAssertJob(jobRequestModel);
    }

    @Test
    void verifyJiraServerSavesTest() {
        JiraServerJobDetailsModel jiraServerJobDetailsModel = new JiraServerJobDetailsModel(
            UUID.randomUUID(),
            true,
            "issueCreatorUsername",
            "projectNameOrKey",
            "issueType",
            "resolveTransition",
            "reopenTransition",
            List.of(),
            "issueSummary"
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(jiraServerJobDetailsModel, new JiraServerChannelKey());
        createAndAssertJob(jobRequestModel);
    }

    @Test
    void verifyJiraCloudSavesTest() {
        JiraCloudJobDetailsModel jiraCloudJobDetailsModel = new JiraCloudJobDetailsModel(
            UUID.randomUUID(),
            true,
            "issueCreatorEmail",
            "projectNameOrKey",
            "issueType",
            "resolveTransition",
            "reopenTransition",
            List.of(),
            "issueSummary"
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(jiraCloudJobDetailsModel, new JiraServerChannelKey());
        createAndAssertJob(jobRequestModel);
    }

    @Test
    void verifyEmailSavesTest() {
        EmailJobDetailsModel emailJobDetailsModel = new EmailJobDetailsModel(
            UUID.randomUUID(),
            "subjectLine",
            false,
            false,
            "attachmentFileType",
            List.of()
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(emailJobDetailsModel, new EmailChannelKey());
        createAndAssertJob(jobRequestModel);
    }

    @Test
    void verifySlackSavesTest() {
        SlackJobDetailsModel slackJobDetailsModel = new SlackJobDetailsModel(
            UUID.randomUUID(),
            "webhook",
            "channelName",
            "channelUsername"
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(slackJobDetailsModel, new SlackChannelKey());
        createAndAssertJob(jobRequestModel);
    }

    @Test
    void verifyMSTeamsSavesTest() {
        MSTeamsJobDetailsModel msTeamsJobDetailsModel = new MSTeamsJobDetailsModel(
            UUID.randomUUID(),
            "webhook"
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(msTeamsJobDetailsModel, new MsTeamsKey());
        createAndAssertJob(jobRequestModel);
    }

    private void createAndAssertJob(DistributionJobRequestModel jobRequestModel) {
        DistributionJobModel createdJob = staticJobAccessor.createJob(jobRequestModel);
        assertNotNull(createdJob);

        Optional<DistributionJobModel> retrievedJob = staticJobAccessor.getJobById(createdJob.getJobId());
        assertTrue(retrievedJob.isPresent());

        DistributionJobModel job = retrievedJob.get();

        assertNotNull(job.getJobId());
        createdJobIds.add(job.getJobId());

        assertNotNull(job.getChannelGlobalConfigId());
        assertEquals(jobRequestModel.getChannelGlobalConfigId(), job.getChannelGlobalConfigId());
    }

    private DistributionJobRequestModel createJobRequestModel(DistributionJobDetailsModel distributionJobDetailsModel, ChannelKey channelKey) {
        return new DistributionJobRequestModel(
            true,
            channelKey.getDisplayName() + " job",
            FrequencyType.REAL_TIME,
            ProcessingType.DEFAULT,
            channelKey.getUniversalKey(),
            UUID.randomUUID(),
            1L,
            false,
            null,
            null,
            List.of(NotificationType.LICENSE_LIMIT.name()),
            List.of(),
            List.of(),
            List.of(),
            distributionJobDetailsModel
        );
    }
}
