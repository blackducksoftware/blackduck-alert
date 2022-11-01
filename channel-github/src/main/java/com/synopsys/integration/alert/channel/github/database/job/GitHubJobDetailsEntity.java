package com.synopsys.integration.alert.channel.github.database.job;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "alert", name = "github_job_details")
public class GitHubJobDetailsEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;
    @Column(name = "repository_url")
    private String repositoryUrl;
    @Column(name = "pull_request_title_prefix")
    private String pullRequestTitlePrefix;

    public GitHubJobDetailsEntity() {
    }

    public GitHubJobDetailsEntity(final UUID jobId, final String repositoryUrl, final String pullRequestTitlePrefix) {
        this.jobId = jobId;
        this.repositoryUrl = repositoryUrl;
        this.pullRequestTitlePrefix = pullRequestTitlePrefix;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(final UUID jobId) {
        this.jobId = jobId;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(final String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getPullRequestTitlePrefix() {
        return pullRequestTitlePrefix;
    }

    public void setPullRequestTitlePrefix(final String pullRequestTitlePrefix) {
        this.pullRequestTitlePrefix = pullRequestTitlePrefix;
    }
}
