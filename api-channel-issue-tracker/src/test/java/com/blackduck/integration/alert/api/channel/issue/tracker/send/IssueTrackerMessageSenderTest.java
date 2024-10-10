/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.send;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

@ExtendWith(SpringExtension.class)
class IssueTrackerMessageSenderTest {
    private final AlertException TEST_EXCEPTION = new AlertException("Test exception");
    @Mock
    private IssueTrackerIssueResponseModel<String> mockIssueTrackerResponseModel;
    @Mock
    private IssueTrackerIssueCreator<String> mockIssueCreator;
    @Mock
    private IssueTrackerIssueTransitioner<String> mockIssueTransitioner;
    @Mock
    private IssueTrackerIssueCommenter<String> mockIssueCommenter;

    @BeforeEach
    public void init() throws AlertException {
        Mockito.when(mockIssueCreator.createIssueTrackerIssue(Mockito.any())).thenReturn(mockIssueTrackerResponseModel);
        Mockito.when(mockIssueTransitioner.transitionIssue(Mockito.any())).thenReturn(Optional.of(mockIssueTrackerResponseModel));
        Mockito.when(mockIssueCommenter.commentOnIssue(Mockito.any())).thenReturn(Optional.of(mockIssueTrackerResponseModel));
    }

    @Test
    void sendMessagesTest() throws AlertException {
        IssueTrackerModelHolder<String> messages = createModelHolder();

        IssueTrackerMessageSender<String> messageSender = new IssueTrackerMessageSender<>(
            mockIssueCreator,
            mockIssueTransitioner,
            mockIssueCommenter
        );
        List<IssueTrackerIssueResponseModel<String>> responseModels = messageSender.sendMessages(messages);
        assertEquals(
            messages.getIssueCreationModels().size() + messages.getIssueTransitionModels().size() + messages.getIssueCommentModels().size(),
            responseModels.size()
        );
    }

    @Test
    void sendMessagesThrowsExceptionTest() throws AlertException {
        IssueTrackerModelHolder<String> messages = createModelHolder();

        IssueTrackerIssueCreator<String> exceptionCreator = Mockito.mock(IssueTrackerIssueCreator.class);
        Mockito.when(exceptionCreator.createIssueTrackerIssue(Mockito.any())).thenThrow(TEST_EXCEPTION);

        IssueTrackerMessageSender<String> messageSender1 = new IssueTrackerMessageSender<>(
            exceptionCreator,
            mockIssueTransitioner,
            mockIssueCommenter
        );
        assertExceptionThrown(messageSender1, messages);

        IssueTrackerIssueCommenter<String> exceptionCommenter = Mockito.mock(IssueTrackerIssueCommenter.class);
        Mockito.when(exceptionCommenter.commentOnIssue(Mockito.any())).thenThrow(TEST_EXCEPTION);

        IssueTrackerMessageSender<String> messageSender2 = new IssueTrackerMessageSender<>(
            mockIssueCreator,
            mockIssueTransitioner,
            exceptionCommenter
        );
        assertExceptionThrown(messageSender2, messages);
    }

    @Test
    void sendMessageIssueCreationTest() throws AlertException {
        IssueCreationModel createModel = IssueCreationModel.simple("tile", "description", List.of(), new LinkableItem("Label", "Value"));
        IssueTrackerMessageSender<String> messageSender = new IssueTrackerMessageSender<>(
            mockIssueCreator,
            mockIssueTransitioner,
            mockIssueCommenter
        );
        List<IssueTrackerIssueResponseModel<String>> createResponses = messageSender.sendMessage(createModel);
        assertEquals(1, createResponses.size());
        assertEquals(mockIssueTrackerResponseModel, createResponses.get(0));
    }

    @Test
    void sendMessageIssueTransitionTest() throws AlertException {
        IssueTransitionModel<String> transitionModel = new IssueTransitionModel<>(null, IssueOperation.RESOLVE, List.of(), null);
        IssueTrackerMessageSender<String> messageSender = new IssueTrackerMessageSender<>(
            mockIssueCreator,
            mockIssueTransitioner,
            mockIssueCommenter
        );
        List<IssueTrackerIssueResponseModel<String>> transitionResponses = messageSender.sendMessage(transitionModel);
        assertEquals(1, transitionResponses.size());
        assertEquals(mockIssueTrackerResponseModel, transitionResponses.get(0));
    }

    @Test
    void sendMessageIssueCommentTest() throws AlertException {
        IssueCommentModel<String> commentModel = new IssueCommentModel<>(null, List.of("Comment 1", "Comment 2"), null);
        IssueTrackerMessageSender<String> messageSender = new IssueTrackerMessageSender<>(
            mockIssueCreator,
            mockIssueTransitioner,
            mockIssueCommenter
        );
        List<IssueTrackerIssueResponseModel<String>> commentResponses = messageSender.sendMessage(commentModel);
        assertEquals(1, commentResponses.size());
        assertEquals(mockIssueTrackerResponseModel, commentResponses.get(0));
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
