/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server;

import com.synopsys.integration.alert.channel.jira.common.model.JiraIssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;

public class JiraServerContext extends IssueTrackerContext {
    public JiraServerContext(JiraServerProperties issueTrackerConfig, JiraIssueConfig issueConfig) {
        super(issueTrackerConfig, issueConfig);
    }

    @Override
    public JiraIssueConfig getIssueConfig() {
        return (JiraIssueConfig) super.getIssueConfig();
    }

}
