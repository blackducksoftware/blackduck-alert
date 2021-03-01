/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.service;

import java.util.Optional;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;

public interface TestIssueRequestCreator {
    // This method could create a creation request or a resolution request.
    Optional<IssueTrackerRequest> createRequest(IssueOperation operation, String messageId);
}
