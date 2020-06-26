package com.synopsys.integration.alert.issuetracker.service.util;

import com.synopsys.integration.alert.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.issuetracker.config.IssueTrackerContext;

public class TestContext extends IssueTrackerContext<TestServerConfig> {
    public TestContext(TestServerConfig issueTrackerConfig, IssueConfig issueConfig) {
        super(issueTrackerConfig, issueConfig);
    }
}
