package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.UUID;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

public class GitHubJobDetailsModel extends DistributionJobDetailsModel {
    private final String repositoryName;
    private final String pullRequestTitlePrefix;

    public GitHubJobDetailsModel(UUID jobId, String repositoryName, String pullRequestTitlePrefix) {
        super(ChannelKeys.GITHUB, jobId);
        this.repositoryName = repositoryName;
        this.pullRequestTitlePrefix = pullRequestTitlePrefix;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getPullRequestTitlePrefix() {
        return pullRequestTitlePrefix;
    }
}
