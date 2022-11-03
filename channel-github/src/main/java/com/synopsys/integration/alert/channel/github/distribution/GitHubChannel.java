package com.synopsys.integration.alert.channel.github.distribution;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.DistributionChannel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.github.database.accessor.GitHubGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.github.model.GitHubGlobalConfigModel;
import com.synopsys.integration.alert.channel.github.service.GitHubService;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.GitHubJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

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
        GitHubJobDetailsModel distributionDetails,
        ProviderMessageHolder messages,
        String jobName,
        UUID eventId,
        Set<Long> notificationIds
    )
        throws AlertException {

        String apiToken = getApiToken(distributionDetails);

        try {
            GitHubService githubService = new GitHubService(apiToken);
            Optional<GHRepository> optionalGHRepository = githubService.getGithubRepository(distributionDetails.getRepositoryName()); //TODO: Use repository name here instead of toString

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
        String defaultBranch = ghRepository.getDefaultBranch();
        GHRef defaultBranchGHRef = ghRepository.getRef(String.format("refs/heads/%s", defaultBranch));
        // Pull the file from default branch -- 1
        GHContent defaultBranchGHContent = ghRepository.getFileContent(GRADLE_FILENAME, defaultBranch);
        // Modify file -- 2
        String fileChanges = getRemediatedChanges(githubService, messages, defaultBranchGHContent);
        // Create commit -- 3
        Optional<GHCommit> optionalGHCommit = githubService.createCommit(ghRepository, defaultBranchGHRef, GRADLE_FILENAME, fileChanges, "Remediation commit");
        if (optionalGHCommit.isEmpty()) {
            return createErrorMessageResult("Could not commit the changes");
        }
        // Create new branch off default -- 4     NOTE: This will create a remote branch in github
        Optional<GHRef> optionalNewBranchRef = githubService.createNewBranchOffDefault(ghRepository, "BlackDuck-Alert-branch");
        if (optionalNewBranchRef.isEmpty()) {
            return createErrorMessageResult("Could not create a new branch");
        }
        GHRef newBranchRef = optionalNewBranchRef.get();
        // Push commit to new branch -- 5
        githubService.pushCommit(newBranchRef, optionalGHCommit.get());
        // Create pull request -- 6
        Optional<GHPullRequest> pullRequestOptional = githubService.createPullRequest(
            ghRepository,
            newBranchRef,
            githubService.getDefaultBranch(ghRepository),
            "BlackDuck Alert remediation pull request",
            "Guidance version upgrades"
        );
        if (pullRequestOptional.isEmpty()) {
            return createErrorMessageResult("Could not create the pull request");
        }
        return MessageResult.success();
    }

    //Updates old versions in the file with new versions
    private String getRemediatedChanges(GitHubService githubService, ProviderMessageHolder messages, GHContent originalGHContent) throws IOException {
        List<BomComponentDetails> componentDetailsList = messages.getProjectMessages().stream()
            .map(ProjectMessage::getBomComponents)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        Map<String, Optional<String>> oldToNewVersionMap = new HashMap<>();
        componentDetailsList.forEach(bomComponentDetails -> oldToNewVersionMap.put(
                bomComponentDetails.getComponentUpgradeGuidance().getOriginExternalId(),
                githubService.getUpgradeGuidanceVersion(bomComponentDetails)
            )
        );

        String fileContent = new String(originalGHContent.read().readAllBytes(), StandardCharsets.UTF_8);
        for (Map.Entry<String, Optional<String>> entry : oldToNewVersionMap.entrySet()) {
            if (entry.getValue().isPresent()) {
                fileContent = githubService.editGithubContentWithNewDependency(fileContent, entry.getKey(), entry.getValue().get());
            }
        }

        return fileContent;
    }
}