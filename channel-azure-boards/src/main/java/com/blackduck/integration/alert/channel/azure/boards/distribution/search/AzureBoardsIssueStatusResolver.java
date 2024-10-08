/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

import com.synopsys.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;

public class AzureBoardsIssueStatusResolver {
    private final String workItemCompletedState;
    private final String workItemReopenState;

    public AzureBoardsIssueStatusResolver(String workItemCompletedState, String workItemReopenState) {
        this.workItemCompletedState = workItemCompletedState;
        this.workItemReopenState = workItemReopenState;
    }

    public IssueStatus resolveIssueStatus(String workItemState) {
        if (workItemState.equals(workItemCompletedState)) {
            return IssueStatus.REOPENABLE;
        } else if (workItemState.equals(workItemReopenState)) {
            return IssueStatus.RESOLVABLE;
        }
        return IssueStatus.UNKNOWN;
    }
}
