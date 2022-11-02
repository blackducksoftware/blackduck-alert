package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.UUID;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

public class GitHubJobDetailsModel extends DistributionJobDetailsModel {
    private final String repositoryUrl;
    private final String pullRequestTitlePrefix;

    public GitHubJobDetailsModel(UUID jobId, String repositoryUrl, String pullRequestTitlePrefix) {
        super(ChannelKeys.GITHUB, jobId);
        this.repositoryUrl = repositoryUrl;
        this.pullRequestTitlePrefix = pullRequestTitlePrefix;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getPullRequestTitlePrefix() {
        return pullRequestTitlePrefix;
    }
}
