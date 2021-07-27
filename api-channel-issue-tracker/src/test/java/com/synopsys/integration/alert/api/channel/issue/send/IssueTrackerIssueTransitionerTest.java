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

import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueMissingTransitionException;

public class IssueTrackerIssueTransitionerTest {
    private static final IssueMissingTransitionException TEST_EXCEPTION = new IssueMissingTransitionException("key", "missing transition", List.of("valid transition"));

    private static IssueTrackerIssueCommenter<String> commenter;

    @BeforeAll
    public static void init() {
        commenter = Mockito.mock(IssueTrackerIssueCommenter.class);
    }

    @Test
    public void transitionIssueTest() throws AlertException {
        IssueOperation testOperation = IssueOperation.RESOLVE;
        ExistingIssueDetails<String> existingIssueDetails = new ExistingIssueDetails<>("id", "key", "title", "https://link", IssueStatus.UNKNOWN, IssueCategory.BOM);
        IssueTransitionModel<String> issueTransitionModel = new IssueTransitionModel<>(existingIssueDetails, testOperation, List.of("comment 1"), null);

        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueTrackerIssueResponseCreator issueResponseCreator = new IssueTrackerIssueResponseCreator(callbackInfoCreator);

        IssueTrackerIssueTransitioner<String> transitioner = new TestTransitioner(commenter, issueResponseCreator, "TODO", true, false);
        Optional<IssueTrackerIssueResponseModel<String>> optionalResponseModel = transitioner.transitionIssue(issueTransitionModel);
        assertTrue(optionalResponseModel.isPresent(), "Expected response model to be present");

        IssueTrackerIssueResponseModel<String> responseModel = optionalResponseModel.get();
        assertEquals(existingIssueDetails.getIssueId(), responseModel.getIssueId());
        assertEquals(existingIssueDetails.getIssueKey(), responseModel.getIssueKey());
        assertEquals(existingIssueDetails.getIssueSummary(), responseModel.getIssueTitle());
        assertEquals(existingIssueDetails.getIssueUILink(), responseModel.getIssueLink());
        assertEquals(testOperation, responseModel.getIssueOperation());
    }

    @Test
    public void transitionIssueNoTransitionRequiredTest() throws AlertException {
        IssueTransitionModel<String> issueTransitionModel = new IssueTransitionModel<>(null, IssueOperation.OPEN, List.of("comment 1"), null);
        IssueTrackerIssueTransitioner<String> transitioner = new TestTransitioner(commenter, null, "Irrelevant name", false, false);
        Optional<IssueTrackerIssueResponseModel<String>> optionalResponseModel = transitioner.transitionIssue(issueTransitionModel);
        assertTrue(optionalResponseModel.isEmpty(), "Expected response model to be empty");
    }

    @Test
    public void transitionIssueNoTransitionNameTest() throws AlertException {
        IssueTransitionModel<String> issueTransitionModel = new IssueTransitionModel<>(null, IssueOperation.OPEN, List.of("comment 1"), null);
        IssueTrackerIssueTransitioner<String> transitioner = new TestTransitioner(commenter, null, null, false, false);
        Optional<IssueTrackerIssueResponseModel<String>> optionalResponseModel = transitioner.transitionIssue(issueTransitionModel);
        assertTrue(optionalResponseModel.isEmpty(), "Expected response model to be empty");
    }

    @Test
    public void transitionIssueThrowsExceptionTest() throws AlertException {
        IssueOperation testOperation = IssueOperation.RESOLVE;
        IssueTransitionModel<String> issueTransitionModel = new IssueTransitionModel<>(null, testOperation, List.of("comment 1"), null);

        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueTrackerIssueResponseCreator issueResponseCreator = new IssueTrackerIssueResponseCreator(callbackInfoCreator);

        IssueTrackerIssueCommenter<String> exceptionThrowingCommenter = Mockito.mock(IssueTrackerIssueCommenter.class);
        Mockito.when(exceptionThrowingCommenter.commentOnIssue(Mockito.any())).thenThrow(new AlertException("Test exception"));

        IssueTrackerIssueTransitioner<String> transitioner = new TestTransitioner(exceptionThrowingCommenter, issueResponseCreator, "TODO", true, true);
        try {
            transitioner.transitionIssue(issueTransitionModel);
            fail("Expected an exception to be thrown");
        } catch (IssueMissingTransitionException e) {
            assertEquals(TEST_EXCEPTION, e);
        }
    }

    private static class TestTransitioner extends IssueTrackerIssueTransitioner<String> {
        private final String jobTransitionName;
        private final boolean isTransitionRequired;
        private final boolean throwException;

        public TestTransitioner(IssueTrackerIssueCommenter commenter, IssueTrackerIssueResponseCreator issueResponseCreator, @Nullable String jobTransitionName, boolean isTransitionRequired, boolean throwException) {
            super(commenter, issueResponseCreator);
            this.jobTransitionName = jobTransitionName;
            this.isTransitionRequired = isTransitionRequired;
            this.throwException = throwException;
        }

        @Override
        protected Optional<String> retrieveJobTransitionName(IssueOperation issueOperation) {
            return Optional.ofNullable(jobTransitionName);
        }

        @Override
        protected boolean isTransitionRequired(ExistingIssueDetails existingIssueDetails, IssueOperation issueOperation) {
            return isTransitionRequired;
        }

        @Override
        protected void findAndPerformTransition(ExistingIssueDetails existingIssueDetails, String transitionName) throws AlertException {
            if (throwException) {
                throw TEST_EXCEPTION;
            }
        }

    }

}
