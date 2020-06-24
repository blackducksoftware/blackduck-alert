package com.synopsys.integration.issuetracker.common.service.util;

import com.synopsys.integration.issuetracker.common.config.IssueConfig;
import com.synopsys.integration.issuetracker.common.config.IssueTrackerContext;

public class TestContext extends IssueTrackerContext<TestServerConfig> {
    public TestContext(final TestServerConfig issueTrackerConfig, final IssueConfig issueConfig) {
        super(issueTrackerConfig, issueConfig);
    }
}
