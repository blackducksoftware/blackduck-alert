/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.send;

import java.io.Serializable;

import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;

public class IssueTrackerIssueResponseCreator<T extends Serializable> {
    private final AlertIssueOriginCreator alertIssueOriginCreator;

    public IssueTrackerIssueResponseCreator(AlertIssueOriginCreator alertIssueOriginCreator) {
        this.alertIssueOriginCreator = alertIssueOriginCreator;
    }

    public final IssueTrackerIssueResponseModel createIssueResponse(ProjectIssueModel source, ExistingIssueDetails<T> existingIssueDetails, IssueOperation issueOperation) {
        AlertIssueOrigin alertIssueOrigin = alertIssueOriginCreator.createIssueOrigin(source);

        return new IssueTrackerIssueResponseModel(
            alertIssueOrigin,
            existingIssueDetails.getIssueKey(),
            existingIssueDetails.getIssueUILink(),
            existingIssueDetails.getIssueSummary(),
            issueOperation
        );
    }

}
