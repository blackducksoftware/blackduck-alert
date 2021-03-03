/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.config;

public class IssueTrackerContext {
    private final IssueTrackerServiceConfig issueTrackerConfig;
    private final IssueConfig issueConfig;

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
