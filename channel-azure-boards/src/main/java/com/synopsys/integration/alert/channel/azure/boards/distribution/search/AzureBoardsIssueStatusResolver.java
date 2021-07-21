package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;

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
