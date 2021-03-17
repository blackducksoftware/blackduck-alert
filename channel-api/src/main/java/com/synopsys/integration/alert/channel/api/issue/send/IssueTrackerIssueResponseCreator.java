/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.send;

import java.io.Serializable;

import com.synopsys.integration.alert.channel.api.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.channel.api.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;

public class IssueTrackerIssueResponseCreator {
    private final IssueTrackerCallbackInfoCreator callbackInfoCreator;

    public IssueTrackerIssueResponseCreator(IssueTrackerCallbackInfoCreator callbackInfoCreator) {
        this.callbackInfoCreator = callbackInfoCreator;
    }

    public final <T extends Serializable> IssueTrackerIssueResponseModel createIssueResponse(ProjectIssueModel source, ExistingIssueDetails<T> existingIssueDetails, IssueOperation issueOperation) {
        IssueTrackerCallbackInfo callbackInfo = callbackInfoCreator.createCallbackInfo(source);
        return new IssueTrackerIssueResponseModel(
            existingIssueDetails.getIssueKey(),
            existingIssueDetails.getIssueUILink(),
            existingIssueDetails.getIssueSummary(),
            issueOperation,
            callbackInfo
        );
    }

}
