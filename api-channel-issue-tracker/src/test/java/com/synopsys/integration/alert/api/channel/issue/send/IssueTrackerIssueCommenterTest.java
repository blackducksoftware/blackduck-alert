package com.synopsys.integration.alert.api.channel.issue.send;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;

public class IssueTrackerIssueCommenterTest {
    private static final AlertException TEST_EXCEPTION = new AlertException("Test exception");

    private static final ExistingIssueDetails<String> EXISTING_ISSUE_DETAILS = new ExistingIssueDetails<>("id", "key", "summary", "https://ui-link", IssueStatus.UNKNOWN, IssueCategory.BOM);
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
    public void commentOnIssueTest() throws AlertException {
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
    public void commentOnIssueDisabledTest() throws AlertException {
        TestCommenter commenter = new TestCommenter(responseCreator, false, false);
        Optional<IssueTrackerIssueResponseModel<String>> responseModel = commenter.commentOnIssue(COMMENT_MODEL);
        assertTrue(responseModel.isEmpty(), "Expected response model to be empty");
    }

    @Test
    public void commentOnIssueThrowsExceptionTest() {
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
