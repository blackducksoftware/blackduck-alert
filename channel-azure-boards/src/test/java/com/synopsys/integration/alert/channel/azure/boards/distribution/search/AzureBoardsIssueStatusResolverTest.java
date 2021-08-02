package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;

public class AzureBoardsIssueStatusResolverTest {
    private final String workItemCompletedState = "Done";
    private final String workItemReopenState = "To Do";

    @Test
    public void resolveIssueStatusTest() {
        String workItemComplete = "Done";
        String workItemReopen = "To Do";
        String workItemUnknown = "Unknown State";

        AzureBoardsIssueStatusResolver azureBoardsIssueStatusResolver = new AzureBoardsIssueStatusResolver(workItemCompletedState, workItemReopenState);

        assertEquals(IssueStatus.REOPENABLE, azureBoardsIssueStatusResolver.resolveIssueStatus(workItemComplete));
        assertEquals(IssueStatus.RESOLVABLE, azureBoardsIssueStatusResolver.resolveIssueStatus(workItemReopen));
        assertEquals(IssueStatus.UNKNOWN, azureBoardsIssueStatusResolver.resolveIssueStatus(workItemUnknown));
    }
}
