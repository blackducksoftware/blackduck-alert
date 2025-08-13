/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
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
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

@AlertIntegrationTest
class StaticJobAccessorTestIT {
    private static final List<UUID> createdJobIds = new LinkedList<>();

    @Autowired
    private StaticJobAccessor staticJobAccessor;

    @AfterEach
    public void cleanup() {
        createdJobIds.forEach(staticJobAccessor::deleteJob);
        createdJobIds.clear();
    }

    @Test
    void verifyAzureSavesTest() {
        AzureBoardsJobDetailsModel azureBoardsJobDetailsModel = new AzureBoardsJobDetailsModel(
            UUID.randomUUID(),
            true,
            "projectNameOrId",
            "workItemType",
            "workItemCompletedState",
            "workItemReopenState"
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(azureBoardsJobDetailsModel, ChannelKeys.AZURE_BOARDS);
        createAndAssertJob(jobRequestModel);
    }

    @Test
    void verifyJiraServerSavesTest() {
        JiraServerJobDetailsModel jiraServerJobDetailsModel = new JiraServerJobDetailsModel(
            UUID.randomUUID(),
            "issueCreatorUsername",
            "projectNameOrKey",
            "issueType",
            "resolveTransition",
            "reopenTransition",
            List.of(),
            "issueSummary"
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(jiraServerJobDetailsModel, ChannelKeys.JIRA_SERVER);
        createAndAssertJob(jobRequestModel);
    }

    @Test
    void verifyJiraCloudSavesTest() {
        JiraCloudJobDetailsModel jiraCloudJobDetailsModel = new JiraCloudJobDetailsModel(
            UUID.randomUUID(),
            "issueCreatorEmail",
            "projectNameOrKey",
            "issueType",
            "resolveTransition",
            "reopenTransition",
            List.of(),
            "issueSummary"
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(jiraCloudJobDetailsModel, ChannelKeys.JIRA_CLOUD);
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

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(emailJobDetailsModel, ChannelKeys.EMAIL);
        createAndAssertJob(jobRequestModel);
    }

    @Test
    void verifySlackSavesTest() {
        SlackJobDetailsModel slackJobDetailsModel = new SlackJobDetailsModel(
            UUID.randomUUID(),
            "webhook",
            "channelUsername"
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(slackJobDetailsModel, ChannelKeys.SLACK);
        createAndAssertJob(jobRequestModel);
    }

    @Test
    void verifyMSTeamsSavesTest() {
        MSTeamsJobDetailsModel msTeamsJobDetailsModel = new MSTeamsJobDetailsModel(
            UUID.randomUUID(),
            "webhook"
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(msTeamsJobDetailsModel, ChannelKeys.MS_TEAMS);
        createAndAssertJob(jobRequestModel);
    }

    @Test
    void verifySearchAndAscendingSortTest() {
        List<String> descriptorNames = createMultipleJobs().stream()
            .map(DescriptorKey::getUniversalKey)
            .collect(Collectors.toList());

        AlertPagedModel<DistributionJobModel> pageOfJobs = staticJobAccessor.getPageOfJobs(0, 10, "", "name", "asc", descriptorNames);
        assertEquals(3, pageOfJobs.getModels().size());
        assertTrue(pageOfJobs.getModels().get(0).getDistributionJobDetails().isA(ChannelKeys.EMAIL));
        assertTrue(pageOfJobs.getModels().get(1).getDistributionJobDetails().isA(ChannelKeys.MS_TEAMS));
        assertTrue(pageOfJobs.getModels().get(2).getDistributionJobDetails().isA(ChannelKeys.SLACK));
    }

    @Test
    void verifySearchAndDescendingSortTest() {
        List<String> descriptorNames = createMultipleJobs().stream()
            .map(DescriptorKey::getUniversalKey)
            .collect(Collectors.toList());

        AlertPagedModel<DistributionJobModel> pageOfJobs = staticJobAccessor.getPageOfJobs(0, 10, "", "name", "desc", descriptorNames);
        assertEquals(3, pageOfJobs.getModels().size());
        assertTrue(pageOfJobs.getModels().get(2).getDistributionJobDetails().isA(ChannelKeys.EMAIL));
        assertTrue(pageOfJobs.getModels().get(1).getDistributionJobDetails().isA(ChannelKeys.MS_TEAMS));
        assertTrue(pageOfJobs.getModels().get(0).getDistributionJobDetails().isA(ChannelKeys.SLACK));
    }

    @Test
    void verifySearchAndSortTest() {
        List<String> descriptorNames = createMultipleJobs().stream()
            .map(DescriptorKey::getUniversalKey)
            .collect(Collectors.toList());

        AlertPagedModel<DistributionJobModel> pageOfJobs = staticJobAccessor.getPageOfJobs(0, 10, "MS Teams", "name", "desc", descriptorNames);
        assertEquals(1, pageOfJobs.getModels().size());
        assertTrue(pageOfJobs.getModels().get(0).getDistributionJobDetails().isA(ChannelKeys.MS_TEAMS));
    }

    private List<DescriptorKey> createMultipleJobs() {
        List<DescriptorKey> descriptorKeys = new ArrayList<>();
        descriptorKeys.add(new BlackDuckProviderKey());
        MSTeamsJobDetailsModel msTeamsJobDetailsModel = new MSTeamsJobDetailsModel(
            UUID.randomUUID(),
            "webhook"
        );

        DistributionJobRequestModel jobRequestModel = createJobRequestModel(msTeamsJobDetailsModel, ChannelKeys.MS_TEAMS);
        descriptorKeys.add(ChannelKeys.MS_TEAMS);
        createAndAssertJob(jobRequestModel);

        SlackJobDetailsModel slackJobDetailsModel = new SlackJobDetailsModel(
            UUID.randomUUID(),
            "webhook",
            "channelUsername"
        );

        jobRequestModel = createJobRequestModel(slackJobDetailsModel, ChannelKeys.SLACK);
        descriptorKeys.add(ChannelKeys.SLACK);
        createAndAssertJob(jobRequestModel);

        EmailJobDetailsModel emailJobDetailsModel = new EmailJobDetailsModel(
            UUID.randomUUID(),
            "subjectLine",
            false,
            false,
            "attachmentFileType",
            List.of()
        );

        jobRequestModel = createJobRequestModel(emailJobDetailsModel, ChannelKeys.EMAIL);
        descriptorKeys.add(ChannelKeys.EMAIL);
        createAndAssertJob(jobRequestModel);

        return descriptorKeys;
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
