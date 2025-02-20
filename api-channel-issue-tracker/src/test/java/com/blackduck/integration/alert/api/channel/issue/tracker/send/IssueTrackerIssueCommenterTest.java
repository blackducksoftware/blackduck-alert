/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;

class IssueTrackerIssueCommenterTest {
    private static final AlertException TEST_EXCEPTION = new AlertException("Test exception");

    private static final ExistingIssueDetails<String> EXISTING_ISSUE_DETAILS = new ExistingIssueDetails<>(
        "id",
        "key",
        "summary",
        "https://ui-link",
        IssueStatus.UNKNOWN,
        IssueCategory.BOM
    );
    private static final IssueCommentModel<String> COMMENT_MODEL = new IssueCommentModel<>(EXISTING_ISSUE_DETAILS, List.of("Comment 1", "Comment 2"), null);
    private static final IssueTrackerIssueResponseModel<String> ISSUE_RESPONSE_MODEL = new IssueTrackerIssueResponseModel<>(
        EXISTING_ISSUE_DETAILS.getIssueId(),
        EXISTING_ISSUE_DETAILS.getIssueKey(),
        EXISTING_ISSUE_DETAILS.getIssueUILink(),
        EXISTING_ISSUE_DETAILS.getIssueSummary(),
        null,
        null
    );

    private static IssueTrackerIssueResponseCreator responseCreator;

    @BeforeAll
    public static void init() {
        responseCreator = Mockito.mock(IssueTrackerIssueResponseCreator.class);
        Mockito.when(responseCreator.createIssueResponse(Mockito.any(), Mockito.eq(EXISTING_ISSUE_DETAILS), Mockito.any())).thenReturn(ISSUE_RESPONSE_MODEL);
    }

    @Test
    void commentOnIssueTest() throws AlertException {
        TestCommenter commenter = new TestCommenter(responseCreator, true, false);
        Optional<IssueTrackerIssueResponseModel<String>> optionalResponseModel = commenter.commentOnIssue(COMMENT_MODEL);
        assertTrue(optionalResponseModel.isPresent(), "Expected response model to be present");

        IssueTrackerIssueResponseModel<String> responseModel = optionalResponseModel.get();
        assertEquals(EXISTING_ISSUE_DETAILS.getIssueId(), responseModel.getIssueId());
        assertEquals(EXISTING_ISSUE_DETAILS.getIssueKey(), responseModel.getIssueKey());
        assertEquals(EXISTING_ISSUE_DETAILS.getIssueUILink(), responseModel.getIssueLink());
        assertEquals(EXISTING_ISSUE_DETAILS.getIssueSummary(), responseModel.getIssueTitle());
    }

    @Test
    void commentOnIssueDisabledTest() throws AlertException {
        TestCommenter commenter = new TestCommenter(responseCreator, false, false);
        Optional<IssueTrackerIssueResponseModel<String>> responseModel = commenter.commentOnIssue(COMMENT_MODEL);
        assertTrue(responseModel.isEmpty(), "Expected response model to be empty");
    }

    @Test
    void commentOnIssueThrowsExceptionTest() {
        TestCommenter commenter = new TestCommenter(responseCreator, true, true);
        try {
            commenter.commentOnIssue(COMMENT_MODEL);
            fail("Expected an exception to be thrown");
        } catch (AlertException e) {
            assertEquals(TEST_EXCEPTION, e);
        }
    }

    private static class TestCommenter extends IssueTrackerIssueCommenter<String> {
        private final boolean isCommentingEnabled;
        private final boolean throwException;

        public TestCommenter(IssueTrackerIssueResponseCreator issueResponseCreator, boolean isCommentingEnabled, boolean throwException) {
            super(issueResponseCreator);
            this.isCommentingEnabled = isCommentingEnabled;
            this.throwException = throwException;
        }

        @Override
        protected boolean isCommentingEnabled() {
            return isCommentingEnabled;
        }

        @Override
        protected void addComment(String comment, ExistingIssueDetails<String> existingIssueDetails, @Nullable ProjectIssueModel source) throws AlertException {
            if (throwException) {
                throw TEST_EXCEPTION;
            }
        }

    }

}
