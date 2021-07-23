package com.synopsys.integration.alert.api.channel.issue.send;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;

public class IssueTrackerMessageSenderTest {
    private static final AlertException TEST_EXCEPTION = new AlertException("Test exception");
    private static final IssueTrackerIssueResponseModel<String> DEFAULT_RESPONSE_MODEL = Mockito.mock(IssueTrackerIssueResponseModel.class);
    private static IssueTrackerIssueCreator<String> creator;
    private static IssueTrackerIssueTransitioner<String> transitioner;
    private static IssueTrackerIssueCommenter<String> commenter;

    @BeforeAll
    public static void init() throws AlertException {
        creator = Mockito.mock(IssueTrackerIssueCreator.class);
        Mockito.when(creator.createIssueTrackerIssue(Mockito.any())).thenReturn(DEFAULT_RESPONSE_MODEL);

        transitioner = Mockito.mock(IssueTrackerIssueTransitioner.class);
        Mockito.when(transitioner.transitionIssue(Mockito.any())).thenReturn(Optional.empty());

        commenter = Mockito.mock(IssueTrackerIssueCommenter.class);
        Mockito.when(commenter.commentOnIssue(Mockito.any())).thenReturn(Optional.of(DEFAULT_RESPONSE_MODEL));
    }

    @Test
    public void sendMessagesTest() throws AlertException {
        IssueTrackerModelHolder<String> messages = createModelHolder();

        IssueTrackerMessageSender<String> messageSender = new IssueTrackerMessageSender<>(creator, transitioner, commenter);
        List<IssueTrackerIssueResponseModel<String>> responseModels = messageSender.sendMessages(messages);
        assertEquals(
            messages.getIssueCreationModels().size() + messages.getIssueTransitionModels().size() + messages.getIssueCommentModels().size(),
            responseModels.size()
        );
    }

    @Test
    public void sendMessagesThrowsExceptionTest() throws AlertException {
        IssueTrackerModelHolder<String> messages = createModelHolder();

        IssueTrackerIssueCreator<String> exceptionCreator = Mockito.mock(IssueTrackerIssueCreator.class);
        Mockito.when(exceptionCreator.createIssueTrackerIssue(Mockito.any())).thenThrow(TEST_EXCEPTION);

        IssueTrackerMessageSender<String> messageSender1 = new IssueTrackerMessageSender<>(exceptionCreator, transitioner, commenter);
        assertExceptionThrown(messageSender1, messages);

        IssueTrackerIssueCommenter<String> exceptionCommenter = Mockito.mock(IssueTrackerIssueCommenter.class);
        Mockito.when(exceptionCommenter.commentOnIssue(Mockito.any())).thenThrow(TEST_EXCEPTION);

        IssueTrackerMessageSender<String> messageSender2 = new IssueTrackerMessageSender<>(creator, transitioner, exceptionCommenter);
        assertExceptionThrown(messageSender2, messages);
    }

    private void assertExceptionThrown(IssueTrackerMessageSender<String> messageSender, IssueTrackerModelHolder<String> messages) {
        try {
            messageSender.sendMessages(messages);
            fail("Expected an exception to be thrown");
        } catch (AlertException e) {
            assertEquals(TEST_EXCEPTION, e);
        }
    }

    private IssueTrackerModelHolder<String> createModelHolder() {
        IssueCreationModel issueCreationModel1 = Mockito.mock(IssueCreationModel.class);
        IssueCreationModel issueCreationModel2 = Mockito.mock(IssueCreationModel.class);
        IssueCommentModel<String> issueCommentModel = Mockito.mock(IssueCommentModel.class);
        return new IssueTrackerModelHolder<>(
            List.of(issueCreationModel1, issueCreationModel2),
            List.of(),
            List.of(issueCommentModel)
        );
    }

}
