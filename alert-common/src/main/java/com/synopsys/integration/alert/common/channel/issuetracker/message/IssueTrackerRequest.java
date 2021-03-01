/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.message;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;

public class IssueTrackerRequest {
    private final IssueSearchProperties issueSearchProperties;
    private final IssueOperation operation;
    private final IssueContentModel requestContent;
    private final AlertIssueOrigin alertIssueOrigin;

    public IssueTrackerRequest(IssueOperation operation, IssueSearchProperties issueSearchProperties, IssueContentModel requestContent, AlertIssueOrigin alertIssueOrigin) {
        this.operation = operation;
        this.issueSearchProperties = issueSearchProperties;
        this.requestContent = requestContent;
        this.alertIssueOrigin = alertIssueOrigin;
    }

    public <T extends IssueSearchProperties> T getIssueSearchProperties() {
        return (T) issueSearchProperties;
    }

    public IssueOperation getOperation() {
        return operation;
    }

    public IssueContentModel getRequestContent() {
        return requestContent;
    }

    public AlertIssueOrigin getAlertIssueOrigin() {
        return alertIssueOrigin;
    }

}
