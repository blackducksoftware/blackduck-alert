package com.synopsys.integration.alert.api.channel.issue.action;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueMissingTransitionException;
import com.synopsys.integration.alert.common.message.model.MessageResult;

public class IssueTrackerTransitionFieldModelTestActionTest {
    private static final IssueTrackerTestActionFieldStatusCreator FIELD_STATUS_CREATOR = new IssueTrackerTestActionFieldStatusCreator();
    private static final ExistingIssueDetails<String> EXISTING_ISSUE_DETAILS = new ExistingIssueDetails<>("test-id", "test-key", "a test summary", "https://a-link", IssueStatus.UNKNOWN, IssueCategory.BOM);
    private static final ProjectIssueModel PROJECT_ISSUE_MODEL = ProjectIssueModel.bom(null, null, null, null);
    private static final IssueTrackerIssueResponseModel<String> ISSUE_RESPONSE_MODEL = new IssueTrackerIssueResponseModel<>("issue-id", "issue-key", "https://a-url", "a title", IssueOperation.OPEN, null);

    private static final String EXPECTED_MESSAGE_RESULT = "Expected a message result to be present";
    private static final String EXPECTED_NO_MESSAGE_RESULT = "Expected no message result to be present";
    public static final String EXPECTED_ERRORS = "Expected the message result to have errors";

    @Test
    public void messageSenderReturnsNoResults() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenReturn(List.of());
        testAndAssertHasErrors(messageSender);
    }

    @Test
    public void messageSenderReturnsResult() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenReturn(List.of(ISSUE_RESPONSE_MODEL));

        IssueTrackerTransitionTestAction<String> transitionTestAction = new IssueTrackerTransitionTestAction<>(messageSender, FIELD_STATUS_CREATOR);

        Optional<MessageResult> optionalMessageResult = transitionTestAction.transitionTestIssueOrReturnFailureResult(IssueOperation.OPEN, EXISTING_ISSUE_DETAILS, PROJECT_ISSUE_MODEL);
        assertTrue(optionalMessageResult.isEmpty(), EXPECTED_NO_MESSAGE_RESULT);
    }

    @Test
    public void messageSenderThrowsIssueMissingTransitionException() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenThrow(new IssueMissingTransitionException(EXISTING_ISSUE_DETAILS.getIssueKey(), "Missing Transition Name", List.of("Valid Transition Name")));
        testAndAssertHasErrors(messageSender);
    }

    @Test
    public void messageSenderThrowsAlertException() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenThrow(new AlertException("Processing exception"));
        testAndAssertHasErrors(messageSender);
    }

    private static void testAndAssertHasErrors(IssueTrackerMessageSender<String> messageSender) {
        IssueTrackerTransitionTestAction<String> transitionTestAction = new IssueTrackerTransitionTestAction<>(messageSender, FIELD_STATUS_CREATOR);

        Optional<MessageResult> optionalMessageResult = transitionTestAction.transitionTestIssueOrReturnFailureResult(IssueOperation.OPEN, EXISTING_ISSUE_DETAILS, PROJECT_ISSUE_MODEL);
        assertTrue(optionalMessageResult.isPresent(), EXPECTED_MESSAGE_RESULT);
        MessageResult messageResult = optionalMessageResult.get();
        assertTrue(messageResult.hasErrors(), EXPECTED_ERRORS);
    }

}
