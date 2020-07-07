package com.synopsys.integration.alert.common.channel.issuetracker.service.util;

import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;

public class TestContext extends IssueTrackerContext<TestServerConfig> {
    public TestContext(TestServerConfig issueTrackerConfig, IssueConfig issueConfig) {
        super(issueTrackerConfig, issueConfig);
    }
}
