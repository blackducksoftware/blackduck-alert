package com.synopsys.integration.alert.issuetracker.service.util;

import com.synopsys.integration.issuetracker.common.config.IssueConfig;
import com.synopsys.integration.issuetracker.common.config.IssueTrackerContext;

public class TestContext extends IssueTrackerContext<TestServerConfig> {
    public TestContext(TestServerConfig issueTrackerConfig, IssueConfig issueConfig) {
        super(issueTrackerConfig, issueConfig);
    }
}
