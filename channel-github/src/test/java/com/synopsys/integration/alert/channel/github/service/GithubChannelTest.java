package com.synopsys.integration.alert.channel.github.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.github.database.accessor.GitHubGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.github.distribution.GitHubChannel;
import com.synopsys.integration.alert.channel.github.model.GitHubGlobalConfigModel;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.persistence.model.job.details.GitHubJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.UpgradeGuidanceDetails;

class GithubChannelTest {
    @Test
    @Disabled("For hackathon demo, this class should be removed in the future")
    void testDistribute() throws AlertException {
        String accessToken = System.getenv("GITHUB_ACCESS_TOKEN");
        assertNotNull(accessToken, "Access token is null, set GITHUB_ACCESS_TOKEN in the environment.");
        String repositoryName = System.getenv("GITHUB_REPOSITORY_NAME");
        assertNotNull(repositoryName, "Github repository name is null, set GITHUB_REPOSITORY_NAME in the environment.");

        String jobName = "myJobName";
        UUID eventId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        UUID jobId = UUID.randomUUID();
        String pullRequestTitlePrefix = "ALERT-";

        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        GitHubGlobalConfigAccessor gitHubGlobalConfigAccessor = Mockito.mock(GitHubGlobalConfigAccessor.class);
        GitHubChannel gitHubChannel = new GitHubChannel(jobAccessor, gitHubGlobalConfigAccessor);

        //For getApiToken:
        // This job model is ignored
        DistributionJobModel distributionJobModel = createDistributionJobModel();
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));

        GitHubGlobalConfigModel model = new GitHubGlobalConfigModel("123", "globalConfigName", accessToken, Boolean.FALSE, 300L);
        Mockito.when(gitHubGlobalConfigAccessor.getConfiguration(Mockito.any())).thenReturn(Optional.of(model));

        GitHubJobDetailsModel distributionDetails = new GitHubJobDetailsModel(jobId, repositoryName, pullRequestTitlePrefix);
        BomComponentDetails bomComponentDetails = createBomComponentDetailsWithUpgradeGuidance();
        ProjectMessage projectMessage = ProjectMessage.componentConcern(null, null, null, List.of(bomComponentDetails));
        ProviderMessageHolder messages = new ProviderMessageHolder(List.of(projectMessage), List.of());

        MessageResult result = gitHubChannel.distributeMessages(
            distributionDetails,
            messages,
            jobName,
            eventId,
            notificationIds
        );
    }

    private BomComponentDetails createBomComponentDetailsWithUpgradeGuidance() {
        UpgradeGuidanceDetails shortTermUpgradeGuidanceDetails = new UpgradeGuidanceDetails(null, "commons-fileupload:commons-fileupload:1.4.0", null, null);
        UpgradeGuidanceDetails longTermUpgradeGuidanceDetails = new UpgradeGuidanceDetails(null, "commons-fileupload:commons-fileupload:1.4.0", null, null);
        String originExternalId = "commons-fileupload:commons-fileupload:1.2.1";
        String componentName = "";
        String componentVersion = "";
        ComponentUpgradeGuidance componentUpgradeGuidance = new ComponentUpgradeGuidance(
            shortTermUpgradeGuidanceDetails,
            longTermUpgradeGuidanceDetails,
            originExternalId,
            componentName,
            componentVersion
        );
        return new BomComponentDetails(
            null,
            null,
            ComponentVulnerabilities.none(),
            List.of(),
            List.of(),
            null,
            null,
            componentUpgradeGuidance,
            List.of(),
            null
        );
    }

    private DistributionJobModel createDistributionJobModel() {
        return new DistributionJobModelBuilder()
            .channelGlobalConfigId(UUID.randomUUID())
            .name("name")
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .channelDescriptorName("channelName")
            .createdAt(OffsetDateTime.now())
            .blackDuckGlobalConfigId(1L)
            .notificationTypes(List.of("POLICY_OVERRIDE"))
            .build();
    }
}
