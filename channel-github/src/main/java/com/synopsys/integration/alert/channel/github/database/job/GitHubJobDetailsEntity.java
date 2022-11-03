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
    @Column(name = "repository_name")
    private String repositoryName;
    @Column(name = "pull_request_title_prefix")
    private String pullRequestTitlePrefix;

    public GitHubJobDetailsEntity() {
    }

    public GitHubJobDetailsEntity(UUID jobId, String repositoryName, String pullRequestTitlePrefix) {
        this.jobId = jobId;
        this.repositoryName = repositoryName;
        this.pullRequestTitlePrefix = pullRequestTitlePrefix;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryUrl) {
        this.repositoryName = repositoryUrl;
    }

    public String getPullRequestTitlePrefix() {
        return pullRequestTitlePrefix;
    }

    public void setPullRequestTitlePrefix(String pullRequestTitlePrefix) {
        this.pullRequestTitlePrefix = pullRequestTitlePrefix;
    }
}
