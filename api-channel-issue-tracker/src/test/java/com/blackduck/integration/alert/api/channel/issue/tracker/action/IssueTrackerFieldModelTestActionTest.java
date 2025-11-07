/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.action;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.issue.tracker.send.AsyncMessageSender;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerAsyncMessageSender;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSender;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSenderFactory;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.model.IssueTrackerChannelKey;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

class IssueTrackerFieldModelTestActionTest {
    private static final String CLASS_NAME = IssueTrackerFieldModelTestActionTest.class.getSimpleName();
    private static final IssueTrackerChannelKey ISSUE_TRACKER_KEY = new IssueTrackerChannelKey(CLASS_NAME, CLASS_NAME) {};

    public static final String EXPECTED_ERRORS = "Expected the message result to have errors";
    private static final String EXPECTED_NO_ERRORS = "Expected the message result not to have errors";
    private static final String EXPECTED_NO_WARNINGS = "Expected the message result not to have warnings";

    private final DistributionJobModel TEST_JOB_MODEL = createTestJobModel();
    private final IssueTrackerIssueResponseModel<String> TEST_ISSUE_RESPONSE_MODEL = createIssueResponseModel();

    @Test
    void testConfigSendMessagesThrowsException() throws AlertException {
        String testExceptionMessage = "test exception message";
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenThrow(new AlertException(testExceptionMessage));

        IssueTrackerMessageSenderFactory<TestJobDetails, String, IssueTrackerModelHolder<String>> messageSenderFactory = new IssueTrackerMessageSenderFactory<>() {
            @Override
            public IssueTrackerMessageSender<String> createMessageSender(TestJobDetails distributionDetails, UUID globalId) throws AlertException {
                return messageSender;
            }

            @Override
            public AsyncMessageSender<IssueTrackerModelHolder<String>> createAsyncMessageSender(
                TestJobDetails distributionDetails,
                UUID globalId,
                UUID jobExecutionId,
                Set<Long> notificationIds
            )
                throws AlertException {
                return null;
            }
        };
        TestIssueTrackerTestAction issueTrackerTestAction = new TestIssueTrackerTestAction(messageSenderFactory);

        MessageResult messageResult = issueTrackerTestAction.testConfig(TEST_JOB_MODEL, "jobName", null, null);
        assertTrue(messageResult.getStatusMessage().contains(testExceptionMessage), "Expected the message result to contain the expected exception message");
    }

    @Test
    void testConfigSendMessagesReturnsNoIssues() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenReturn(List.of());

        IssueTrackerMessageSenderFactory<TestJobDetails, String,IssueTrackerModelHolder<String>> messageSenderFactory = new IssueTrackerMessageSenderFactory<>() {
            @Override
            public IssueTrackerMessageSender<String> createMessageSender(TestJobDetails distributionDetails, UUID globalId) throws AlertException {
                return messageSender;
            }

            @Override
            public AsyncMessageSender<IssueTrackerModelHolder<String>> createAsyncMessageSender(
                TestJobDetails distributionDetails,
                UUID globalId,
                UUID jobExecutionId,
                Set<Long> notificationIds
            )
                throws AlertException {
                return null;
            }
        };
        TestIssueTrackerTestAction issueTrackerTestAction = new TestIssueTrackerTestAction(messageSenderFactory);

        MessageResult messageResult = issueTrackerTestAction.testConfig(TEST_JOB_MODEL, "jobName", null, null);
        assertTrue(messageResult.hasErrors(), EXPECTED_ERRORS);
    }

    @Test
    void testConfigNoResolveTransition() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenReturn(List.of(TEST_ISSUE_RESPONSE_MODEL));

        IssueTrackerMessageSenderFactory<TestJobDetails, String,IssueTrackerModelHolder<String>> messageSenderFactory = new IssueTrackerMessageSenderFactory<>() {
            @Override
            public IssueTrackerMessageSender<String> createMessageSender(TestJobDetails distributionDetails, UUID globalId) throws AlertException {
                return messageSender;
            }

            @Override
            public AsyncMessageSender<IssueTrackerModelHolder<String>> createAsyncMessageSender(
                TestJobDetails distributionDetails,
                UUID globalId,
                UUID jobExecutionId,
                Set<Long> notificationIds
            )
                throws AlertException {
                return null;
            }
        };
        TestIssueTrackerTestAction issueTrackerTestAction = new TestIssueTrackerTestAction(messageSenderFactory);

        MessageResult messageResult = issueTrackerTestAction.testConfig(TEST_JOB_MODEL, "jobName", null, null);
        assertFalse(messageResult.hasErrors(), EXPECTED_NO_ERRORS);
        assertFalse(messageResult.hasWarnings(), EXPECTED_NO_WARNINGS);
    }

    @Test
    void testConfigResolveFailure() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenAnswer(invocation -> {
            IssueTrackerModelHolder<String> argument = invocation.getArgument(0);
            if (!argument.getIssueCreationModels().isEmpty()) {
                return List.of(TEST_ISSUE_RESPONSE_MODEL);
            }
            return List.of();
        });

        IssueTrackerMessageSenderFactory<TestJobDetails, String, IssueTrackerModelHolder<String>> messageSenderFactory = new IssueTrackerMessageSenderFactory<>() {
            @Override
            public IssueTrackerMessageSender<String> createMessageSender(TestJobDetails distributionDetails, UUID globalId) throws AlertException {
                return messageSender;
            }

            @Override
            public AsyncMessageSender<IssueTrackerModelHolder<String>> createAsyncMessageSender(
                TestJobDetails distributionDetails,
                UUID globalId,
                UUID jobExecutionId,
                Set<Long> notificationIds
            )
                throws AlertException {
                return null;
            }
        };
        TestIssueTrackerTestAction issueTrackerTestAction = new TestIssueTrackerTestAction(messageSenderFactory, true, false);

        MessageResult messageResult = issueTrackerTestAction.testConfig(TEST_JOB_MODEL, "jobName", null, null);
        assertTrue(messageResult.hasErrors(), EXPECTED_ERRORS);
    }

    @Test
    void testConfigNoReopen() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenReturn(List.of(TEST_ISSUE_RESPONSE_MODEL));

        IssueTrackerMessageSenderFactory<TestJobDetails, String, IssueTrackerModelHolder<String>> messageSenderFactory = new IssueTrackerMessageSenderFactory<>() {
            @Override
            public IssueTrackerMessageSender<String> createMessageSender(TestJobDetails distributionDetails, UUID globalId) throws AlertException {
                return messageSender;
            }

            @Override
            public AsyncMessageSender<IssueTrackerModelHolder<String>> createAsyncMessageSender(
                TestJobDetails distributionDetails,
                UUID globalId,
                UUID jobExecutionId,
                Set<Long> notificationIds
            )
                throws AlertException {
                return null;
            }
        };
        TestIssueTrackerTestAction issueTrackerTestAction = new TestIssueTrackerTestAction(messageSenderFactory, true, false);

        MessageResult messageResult = issueTrackerTestAction.testConfig(TEST_JOB_MODEL, "jobName", null, null);
        assertFalse(messageResult.hasErrors(), EXPECTED_NO_ERRORS);
        assertFalse(messageResult.hasWarnings(), EXPECTED_NO_WARNINGS);
    }

    @Test
    void testConfigReopenFailure() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenAnswer(invocation -> {
            IssueTrackerModelHolder<String> argument = invocation.getArgument(0);
            List<IssueTransitionModel<String>> transitionModels = argument.getIssueTransitionModels();
            if (!transitionModels.isEmpty()) {
                if (transitionModels.stream().anyMatch(model -> IssueOperation.RESOLVE.equals(model.getIssueOperation()))) {
                    return List.of(TEST_ISSUE_RESPONSE_MODEL);
                } else {
                    return List.of();
                }
            }
            return List.of(TEST_ISSUE_RESPONSE_MODEL);
        });

        IssueTrackerMessageSenderFactory<TestJobDetails, String,IssueTrackerModelHolder<String>> messageSenderFactory = new IssueTrackerMessageSenderFactory<>() {
            @Override
            public IssueTrackerMessageSender<String> createMessageSender(TestJobDetails distributionDetails, UUID globalId) throws AlertException {
                return messageSender;
            }

            @Override
            public AsyncMessageSender<IssueTrackerModelHolder<String>> createAsyncMessageSender(
                TestJobDetails distributionDetails,
                UUID globalId,
                UUID jobExecutionId,
                Set<Long> notificationIds
            )
                throws AlertException {
                return null;
            }
        };
        TestIssueTrackerTestAction issueTrackerTestAction = new TestIssueTrackerTestAction(messageSenderFactory, true, true);

        MessageResult messageResult = issueTrackerTestAction.testConfig(TEST_JOB_MODEL, "jobName", null, null);
        assertTrue(messageResult.hasErrors(), EXPECTED_ERRORS);
    }

    private static DistributionJobModel createTestJobModel() {
        return DistributionJobModel.builder()
            .jobId(UUID.randomUUID())
            .name(CLASS_NAME)
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.SUMMARY)
            .channelDescriptorName(ISSUE_TRACKER_KEY.getUniversalKey())
            .createdAt(OffsetDateTime.now())
            .blackDuckGlobalConfigId(0L)
            .notificationTypes(List.of("irrelevant_string"))
            .filterByProject(false)
            .build();
    }

    private static IssueTrackerIssueResponseModel<String> createIssueResponseModel() {
        return new IssueTrackerIssueResponseModel<>("issue-id", "issue-key", "https://a-url", "a title", IssueOperation.OPEN, null);
    }

    private static class TestIssueTrackerTestAction extends IssueTrackerTestAction<TestJobDetails, String> {
        private final boolean hasResolveTransition;
        private final boolean hasReopenTransition;

        public TestIssueTrackerTestAction(IssueTrackerMessageSenderFactory<TestJobDetails, String,IssueTrackerModelHolder<String>> messageSenderFactory) {
            this(messageSenderFactory, false, false);
        }

        public TestIssueTrackerTestAction(IssueTrackerMessageSenderFactory<TestJobDetails, String,IssueTrackerModelHolder<String>> messageSenderFactory, boolean hasResolveTransition, boolean hasReopenTransition) {
            super(ISSUE_TRACKER_KEY, messageSenderFactory);
            this.hasResolveTransition = hasResolveTransition;
            this.hasReopenTransition = hasReopenTransition;
        }

        @Override
        protected boolean hasResolveTransition(TestJobDetails distributionDetails) {
            return hasResolveTransition;
        }

        @Override
        protected boolean hasReopenTransition(TestJobDetails distributionDetails) {
            return hasReopenTransition;
        }

    }

    private static class TestJobDetails extends DistributionJobDetailsModel {
        private final String field1;

        public TestJobDetails(UUID jobId, String field1) {
            super(ISSUE_TRACKER_KEY, jobId);
            this.field1 = field1;
        }

        public String getField1() {
            return field1;
        }

    }

}
