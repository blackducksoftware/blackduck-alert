package com.synopsys.integration.alert.channel.github.distribution;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.github.database.accessor.GitHubGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.github.model.GitHubGlobalConfigModel;
import com.synopsys.integration.alert.channel.github.service.GitHubService;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.DistributionChannel;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.GitHubJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

@Component
public class GitHubChannel implements DistributionChannel<GitHubJobDetailsModel> {
    private final GitHubGlobalConfigAccessor gitHubConfigModelConfigAccessor;
    private final JobAccessor jobAccessor;

    private final String GITHUB_DISTRIBUTION_FAILURE_STATUS_MESSAGE = "Failed distribution for GitHub.";

    @Autowired
    public GitHubChannel(JobAccessor jobAccessor, GitHubGlobalConfigAccessor gitHubConfigModelConfigAccessor) {
        this.jobAccessor = jobAccessor;
        this.gitHubConfigModelConfigAccessor = gitHubConfigModelConfigAccessor;
    }

    @Override
    public MessageResult distributeMessages(
        final GitHubJobDetailsModel distributionDetails,
        final ProviderMessageHolder messages,
        final String jobName,
        final UUID eventId,
        final Set<Long> notificationIds
    )
        throws AlertException {
        // get githubconfigmodel? from distributiondetails -> jobid -> jobaccessor -> channelglobalconfigid -> globalconfigaccessor -> config
        // get token from configmodel, create service
        // Go through github work flow, make changes from bomcomponent
        // providermessageholder has upgrade details project
        DistributionJobModel distributionJobModel = jobAccessor.getJobById(distributionDetails.getJobId()).orElseThrow(
            () -> new AlertConfigurationException("Missing GitHub distribution configuration")
        );
        GitHubGlobalConfigModel configModel = gitHubConfigModelConfigAccessor.getConfiguration(distributionJobModel.getChannelGlobalConfigId()).orElseThrow(
            () -> new AlertConfigurationException("Missing GitHub config model configuration")
        );

        try {
            GitHubService gitHubService = new GitHubService(configModel.getApiToken());
            Optional<GHRepository> optionalGHRepository = gitHubService.getGithubRepository(distributionDetails.toString()); //TODO: Use repository name here instead of toString
            if (optionalGHRepository.isPresent()) {
                GHRepository ghRepository = optionalGHRepository.get();

                // TODO: Implement martin's changes
            } else {
                return new MessageResult(
                    GITHUB_DISTRIBUTION_FAILURE_STATUS_MESSAGE,
                    List.of(AlertFieldStatus.error("none", "Could not find GitHub repository"))
                );
            }

        } catch (IOException e) {
            throw new AlertException(e);
        }

        return MessageResult.success();
    }
}

