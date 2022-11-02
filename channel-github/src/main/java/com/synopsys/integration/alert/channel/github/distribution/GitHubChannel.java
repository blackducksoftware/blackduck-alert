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
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRef;
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
    private static final String GRADLE_FILENAME = "build.gradle";

    private final GitHubGlobalConfigAccessor githubConfigModelConfigAccessor;
    private final JobAccessor jobAccessor;

    @Autowired
    public GitHubChannel(JobAccessor jobAccessor, GitHubGlobalConfigAccessor githubConfigModelConfigAccessor) {
        this.jobAccessor = jobAccessor;
        this.githubConfigModelConfigAccessor = githubConfigModelConfigAccessor;
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

        String apiToken = getApiToken(distributionDetails);

        try {
            GitHubService githubService = new GitHubService(apiToken);
            Optional<GHRepository> optionalGHRepository = githubService.getGithubRepository(distributionDetails.getRepositoryUrl()); //TODO: Use repository name here instead of toString

            if (optionalGHRepository.isEmpty()) {
                return createErrorMessageResult("Could not find GitHub repository");
            }
            GHRepository ghRepository = optionalGHRepository.get();

            return performRemediationProcess(githubService, ghRepository, messages);
        } catch (IOException e) {
            return createErrorMessageResult(e.getMessage());
        }
    }

    private MessageResult createErrorMessageResult(String errorMessage) {
        return new MessageResult(
            "Failed distribution for GitHub.",
            List.of(AlertFieldStatus.error("none", errorMessage))
        );
    }

    private String getApiToken(DistributionJobDetailsModel distributionDetails) throws AlertConfigurationException {
        DistributionJobModel distributionJobModel = jobAccessor.getJobById(distributionDetails.getJobId()).orElseThrow(
            () -> new AlertConfigurationException("Missing GitHub distribution configuration")
        );
        GitHubGlobalConfigModel configModel = githubConfigModelConfigAccessor.getConfiguration(distributionJobModel.getChannelGlobalConfigId()).orElseThrow(
            () -> new AlertConfigurationException("Missing GitHub config configuration")
        );
        return configModel.getApiToken();
    }

    private MessageResult performRemediationProcess(GitHubService githubService, GHRepository ghRepository, ProviderMessageHolder messages) throws IOException {
        Optional<GHRef> optionalNewBranchRef = githubService.createNewBranchOffDefault(ghRepository, "BlackDuck Alert branch");
        if (optionalNewBranchRef.isEmpty()) {
            return createErrorMessageResult("Could not create a new branch");
        }
        GHRef newBranchRef = optionalNewBranchRef.get();

        GHContent originalGHContent = ghRepository.getFileContent(GRADLE_FILENAME, newBranchRef.getRef());
        String fileChanges = getRemediatedChanges(githubService, messages, originalGHContent);

        Optional<GHCommit> optionalGHCommit = githubService.createCommit(ghRepository, newBranchRef, GRADLE_FILENAME, fileChanges, "Remediation commit");
        if (optionalGHCommit.isEmpty()) {
            return createErrorMessageResult("Could not commit the changes");
        }

        githubService.pushCommit(newBranchRef, optionalGHCommit.get());

        if (githubService.createPullRequest(
                ghRepository,
                newBranchRef,
                githubService.getDefaultBranch(ghRepository),
                "BlackDuck Alert remediation pull request",
                "Guidance version upgrades"
            ).isEmpty()
        ) {
            return createErrorMessageResult("Could not create the pull request");
        }

        return MessageResult.success();
    }

    //TODO: implement
    private String getRemediatedChanges(GitHubService githubService, ProviderMessageHolder messages, GHContent originalGHContent) throws IOException {
        githubService.editGithubContentWithNewDependency(originalGHContent, "", "");
        return "";
    }
}

