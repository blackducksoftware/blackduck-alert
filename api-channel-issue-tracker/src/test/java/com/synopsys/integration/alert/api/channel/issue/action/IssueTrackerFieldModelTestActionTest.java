package com.synopsys.integration.alert.api.channel.issue.action;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSenderFactory;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.IssueTrackerChannelKey;

public class IssueTrackerFieldModelTestActionTest {
    private static final String CLASS_NAME = IssueTrackerFieldModelTestActionTest.class.getSimpleName();
    private static final IssueTrackerChannelKey ISSUE_TRACKER_KEY = new IssueTrackerChannelKey(CLASS_NAME, CLASS_NAME) {};

    public static final String EXPECTED_ERRORS = "Expected the message result to have errors";
    private static final String EXPECTED_NO_ERRORS = "Expected the message result not to have errors";
    private static final String EXPECTED_NO_WARNINGS = "Expected the message result not to have warnings";

    private final DistributionJobModel TEST_JOB_MODEL = createTestJobModel();
    private final IssueTrackerIssueResponseModel<String> TEST_ISSUE_RESPONSE_MODEL = createIssueResponseModel();

    @Test
    public void testConfigSendMessagesThrowsException() throws AlertException {
        String testExceptionMessage = "test exception message";
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenThrow(new AlertException(testExceptionMessage));

        IssueTrackerMessageSenderFactory<TestJobDetails, String> messageSenderFactory = distributionDetails -> messageSender;
        TestIssueTrackerTestAction issueTrackerTestAction = new TestIssueTrackerTestAction(messageSenderFactory);

        MessageResult messageResult = issueTrackerTestAction.testConfig(TEST_JOB_MODEL, "jobName", null, null);
        assertTrue(messageResult.getStatusMessage().contains(testExceptionMessage), "Expected the message result to contain the expected exception message");
    }

    @Test
    public void testConfigSendMessagesReturnsNoIssues() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenReturn(List.of());

        IssueTrackerMessageSenderFactory<TestJobDetails, String> messageSenderFactory = distributionDetails -> messageSender;
        TestIssueTrackerTestAction issueTrackerTestAction = new TestIssueTrackerTestAction(messageSenderFactory);

        MessageResult messageResult = issueTrackerTestAction.testConfig(TEST_JOB_MODEL, "jobName", null, null);
        assertTrue(messageResult.hasErrors(), EXPECTED_ERRORS);
    }

    @Test
    public void testConfigNoResolveTransition() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenReturn(List.of(TEST_ISSUE_RESPONSE_MODEL));

        IssueTrackerMessageSenderFactory<TestJobDetails, String> messageSenderFactory = distributionDetails -> messageSender;
        TestIssueTrackerTestAction issueTrackerTestAction = new TestIssueTrackerTestAction(messageSenderFactory);

        MessageResult messageResult = issueTrackerTestAction.testConfig(TEST_JOB_MODEL, "jobName", null, null);
        assertFalse(messageResult.hasErrors(), EXPECTED_NO_ERRORS);
        assertFalse(messageResult.hasWarnings(), EXPECTED_NO_WARNINGS);
    }

    @Test
    public void testConfigResolveFailure() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenAnswer(invocation -> {
            IssueTrackerModelHolder<String> argument = invocation.getArgument(0);
            if (!argument.getIssueCreationModels().isEmpty()) {
                return List.of(TEST_ISSUE_RESPONSE_MODEL);
            }
            return List.of();
        });

        IssueTrackerMessageSenderFactory<TestJobDetails, String> messageSenderFactory = distributionDetails -> messageSender;
        TestIssueTrackerTestAction issueTrackerTestAction = new TestIssueTrackerTestAction(messageSenderFactory, true, false);

        MessageResult messageResult = issueTrackerTestAction.testConfig(TEST_JOB_MODEL, "jobName", null, null);
        assertTrue(messageResult.hasErrors(), EXPECTED_ERRORS);
    }

    @Test
    public void testConfigNoReopen() throws AlertException {
        IssueTrackerMessageSender<String> messageSender = Mockito.mock(IssueTrackerMessageSender.class);
        Mockito.when(messageSender.sendMessages(Mockito.any())).thenReturn(List.of(TEST_ISSUE_RESPONSE_MODEL));

        IssueTrackerMessageSenderFactory<TestJobDetails, String> messageSenderFactory = distributionDetails -> messageSender;
        TestIssueTrackerTestAction issueTrackerTestAction = new TestIssueTrackerTestAction(messageSenderFactory, true, false);

        MessageResult messageResult = issueTrackerTestAction.testConfig(TEST_JOB_MODEL, "jobName", null, null);
        assertFalse(messageResult.hasErrors(), EXPECTED_NO_ERRORS);
        assertFalse(messageResult.hasWarnings(), EXPECTED_NO_WARNINGS);
    }

    @Test
    public void testConfigReopenFailure() throws AlertException {
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

        IssueTrackerMessageSenderFactory<TestJobDetails, String> messageSenderFactory = distributionDetails -> messageSender;
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

        public TestIssueTrackerTestAction(IssueTrackerMessageSenderFactory<TestJobDetails, String> messageSenderFactory) {
            this(messageSenderFactory, false, false);
        }

        public TestIssueTrackerTestAction(IssueTrackerMessageSenderFactory<TestJobDetails, String> messageSenderFactory, boolean hasResolveTransition, boolean hasReopenTransition) {
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
