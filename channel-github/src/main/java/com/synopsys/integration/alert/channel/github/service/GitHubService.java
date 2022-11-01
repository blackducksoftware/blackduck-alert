package com.synopsys.integration.alert.channel.github.service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.kohsuke.github.*;
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

    //TODO: Step 1) Pull the package manager file/repository... should we make this cacheable?
    public String getDefaultBranch(GHRepository githubRepository) {
        return githubRepository.getDefaultBranch();
    }

    // Create a new branch
    public Optional<GHRef> createNewBranchOffDefault(GHRepository githubRepository, String newBranchName) throws IOException {
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

    // Commit the changes to the specified ref
    public GHCommit createCommit(GHRepository githubRepository, GHRef ref, String fileName, String fileContent, String commitMessage) throws IOException {
        GHCommit refLatestCommit = githubRepository.getCommit(ref.getObject().getSha());
        GHTreeBuilder ghTreeBuilder = githubRepository.createTree().baseTree(refLatestCommit.getTree().getSha());
        ghTreeBuilder.add(fileName, fileContent, false);
        GHTree ghTree = ghTreeBuilder.create();
        return githubRepository.createCommit()
                .parent(refLatestCommit.getSHA1())
                .tree(ghTree.getSha())
                .message(commitMessage)
                .create();
    }

    // Push the commit to the ref
    public void pushCommit(GHRef ref, GHCommit commit) throws IOException {
        ref.updateTo(commit.getSHA1());
    }

    // Open a PR from the ref to the specified toBranch
    public GHPullRequest createPullRequest(GHRepository githubRepository, GHRef fromRef, String toBranch, String title, String description) throws IOException {
        return githubRepository.createPullRequest(title, fromRef.getRef(), String.format("refs/heads/%s", toBranch), description);
    }
}
