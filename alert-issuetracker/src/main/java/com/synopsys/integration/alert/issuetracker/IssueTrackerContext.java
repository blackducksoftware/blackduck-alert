package com.synopsys.integration.alert.issuetracker;

public class IssueTrackerContext {
    private IssueTrackerServiceConfig issueTrackerConfig;
    private IssueConfig issueConfig;

    public IssueTrackerContext(IssueTrackerServiceConfig issueTrackerConfig, IssueConfig issueConfig) {
        this.issueTrackerConfig = issueTrackerConfig;
        this.issueConfig = issueConfig;
    }

    public IssueTrackerServiceConfig getIssueTrackerConfig() {
        return issueTrackerConfig;
    }

    public IssueConfig getIssueConfig() {
        return issueConfig;
    }
}
