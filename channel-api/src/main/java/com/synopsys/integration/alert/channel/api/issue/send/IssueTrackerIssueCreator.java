/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.send;

import com.synopsys.integration.alert.channel.api.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.exception.AlertException;

public interface IssueTrackerIssueCreator {
    IssueTrackerIssueResponseModel createIssue(IssueCreationModel issueCreationModel) throws AlertException;

}
