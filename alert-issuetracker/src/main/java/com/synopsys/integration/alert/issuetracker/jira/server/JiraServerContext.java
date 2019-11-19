package com.synopsys.integration.alert.issuetracker.jira.server;

import com.synopsys.integration.alert.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.issuetracker.config.IssueTrackerContext;

public class JiraServerContext extends IssueTrackerContext<JiraServerProperties> {
    public JiraServerContext(JiraServerProperties issueTrackerConfig, IssueConfig issueConfig) {
        super(issueTrackerConfig, issueConfig);
    }
}
