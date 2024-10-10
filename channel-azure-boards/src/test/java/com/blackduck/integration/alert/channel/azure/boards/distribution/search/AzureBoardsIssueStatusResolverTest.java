/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;

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
