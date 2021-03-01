/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.message;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;

public class IssueResolutionRequest extends IssueTrackerRequest {
    public static final IssueOperation OPERATION = IssueOperation.RESOLVE;

    private IssueResolutionRequest(IssueSearchProperties issueSearchProperties, IssueContentModel requestContent, AlertIssueOrigin alertIssueOrigin) {
        super(OPERATION, issueSearchProperties, requestContent, alertIssueOrigin);
    }

    public static final IssueResolutionRequest of(IssueSearchProperties issueSearchProperties, IssueContentModel content, AlertIssueOrigin alertIssueOrigin) {
        return new IssueResolutionRequest(issueSearchProperties, content, alertIssueOrigin);
    }

}
