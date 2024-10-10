/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.model.IssueTrackerChannelKey;

public class IssueTrackerIssueCreatorTest {
    private static final IssueTrackerChannelKey ISSUE_TRACKER_CHANNEL_KEY = new IssueTrackerChannelKey("key", "name") {};

    private static IssueTrackerIssueCommenter<String> commenter;
    private static IssueTrackerCallbackInfoCreator callbackInfoCreator;

    @BeforeAll
    public static void init() throws AlertException {
        commenter = Mockito.mock(IssueTrackerIssueCommenter.class);
        Mockito.when(commenter.commentOnIssue(Mockito.any())).thenReturn(Optional.empty());

        callbackInfoCreator = Mockito.mock(IssueTrackerCallbackInfoCreator.class);
        Mockito.when(callbackInfoCreator.createCallbackInfo(Mockito.any())).thenReturn(Optional.empty());
    }

    @Test
    void createIssueTrackerIssueTest() throws AlertException {
        TestIssueCreator issueCreator = new TestIssueCreator(commenter, callbackInfoCreator);
        IssueCreationModel issueCreationModel = IssueCreationModel.simple("Test title", null, List.of("comment 1", "comment 2"), null);
        IssueTrackerIssueResponseModel<String> responseModel = issueCreator.createIssueTrackerIssue(issueCreationModel);
        assertEquals(issueCreationModel.getTitle(), responseModel.getIssueTitle());
    }

    @Test
    void createIssueTrackerIssueWithSourceTest() throws AlertException {
        TestIssueCreator issueCreator = new TestIssueCreator(commenter, callbackInfoCreator);
        ProjectIssueModel projectIssueModel = Mockito.mock(ProjectIssueModel.class);
        IssueCreationModel issueCreationModel = IssueCreationModel.project("Test title", null, List.of("example comment"), projectIssueModel, null);
        IssueTrackerIssueResponseModel<String> responseModel = issueCreator.createIssueTrackerIssue(issueCreationModel);
        assertEquals(issueCreationModel.getTitle(), responseModel.getIssueTitle());
    }

    private static class TestIssueCreator extends IssueTrackerIssueCreator<String> {
        public TestIssueCreator(IssueTrackerIssueCommenter<String> commenter, IssueTrackerCallbackInfoCreator callbackInfoCreator) {
            super(ISSUE_TRACKER_CHANNEL_KEY, commenter, callbackInfoCreator);
        }

        @Override
        protected ExistingIssueDetails<String> createIssueAndExtractDetails(IssueCreationModel alertIssueCreationModel) {
            return new ExistingIssueDetails<>(null, null, alertIssueCreationModel.getTitle(), null, IssueStatus.UNKNOWN, IssueCategory.BOM);
        }

        @Override
        protected void assignAlertSearchProperties(ExistingIssueDetails<String> createdIssueDetails, ProjectIssueModel alertIssueSource) {
            // Do nothing
        }

    }

}
