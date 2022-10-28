package com.synopsys.integration.alert.api.channel.github;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;

public class GitHubService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GitHub gitHubApiClient;

    public GitHubService(String accessToken) throws IOException {
        gitHubApiClient = buildGithubClient(accessToken);
    }

    public GitHub buildGithubClient(String accessToken) throws IOException {
        return new GitHubBuilder().withOAuthToken(accessToken).build();
    }

    public Optional<GHRepository> getGithubRepository(String repositoryName) {
        try {
            return Optional.of(gitHubApiClient.getRepository(repositoryName));
        } catch (IOException ex) {
            logger.error("Could not find github repository: ", ex);
            return Optional.empty();
        }
    }

    //TODO: Step 1) Pull the package manager file/repository
    public String getDefaultBranch(GHRepository githubRepository) {
        return githubRepository.getDefaultBranch();
    }

    //TODO: Step 2) Create a new branch
    public Optional<GHRef> createNewBranch(GHRepository githubRepository, String newBranchName) throws IOException {
        Map<String, GHBranch> repositoryBranches = githubRepository.getBranches();
        if (repositoryBranches.containsKey(newBranchName)) {
            logger.error(String.format("The branch \"%s\" already exists.", newBranchName));
            return Optional.empty();
        }
        String defaultBranchName = getDefaultBranch(githubRepository);
        String defaultBranchSha = githubRepository.getRef(String.format("refs/heads/%s", defaultBranchName)).getObject().getSha();
        return Optional.of(githubRepository.createRef(String.format("refs/heads/%s", newBranchName), defaultBranchSha));
    }

    //TODO: Step 3) Edit the build.gradle file with remediated version
    public String getRemediatedVersion(BomComponentDetails bomComponentDetails) {

        //TODO: implement
        return null;
    }

    //TODO: Step 4) Commit the changes to the branch

    //TODO: Step 5) Push the branch to the remote

    //TODO: Step 6) Open a PR from the branch into the main development branch (main/master/or development)
}
