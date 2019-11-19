package com.synopsys.integration.alert.issuetracker.jira.cloud;

import com.synopsys.integration.alert.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.issuetracker.config.IssueTrackerContext;

public class JiraCloudContext extends IssueTrackerContext<JiraCloudProperties> {
    public JiraCloudContext(JiraCloudProperties issueTrackerConfig, IssueConfig issueConfig) {
        super(issueTrackerConfig, issueConfig);
    }
}
