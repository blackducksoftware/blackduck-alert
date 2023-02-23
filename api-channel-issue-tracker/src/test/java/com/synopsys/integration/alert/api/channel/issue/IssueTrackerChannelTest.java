package com.synopsys.integration.alert.api.channel.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.SyncTaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.convert.IssueTrackerMessageFormatter;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerAsyncMessageSender;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerCommentEventGenerator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerCreationEventGenerator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerTransitionEventGenerator;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
class IssueTrackerChannelTest {
    @Test
    void distributeMessagesTest() throws AlertException {
        UUID jobId = UUID.randomUUID();
        DistributionJobDetailsModel distributionJobDetailsModel = new DistributionJobDetailsModel(ChannelKeys.SLACK, jobId) {
            private static final long serialVersionUID = 5355069038110415471L;
        };
        IssueTrackerModelExtractor<String> modelExtractor = new IssueTrackerModelExtractor<>(createFormatter(), null);
        JobSubTaskAccessor jobSubTaskAccessor = createJobSubTaskAccessor();
        IssueTrackerAsyncMessageSender<String> messageSender = createMessageSender(jobSubTaskAccessor);
        IssueTrackerProcessor<String> processor = new IssueTrackerProcessor<>(modelExtractor, messageSender);

        IssueTrackerProcessorFactory<DistributionJobDetailsModel, String> processorFactory = (v, x, y, z) -> processor;

        IssueTrackerResponsePostProcessor postProcessor = new IssueTrackerResponsePostProcessor() {
            @Override
            public <T extends Serializable> void postProcess(IssueTrackerResponse<T> response) {
            }
        };
        IssueTrackerChannel<DistributionJobDetailsModel, String> issueTrackerChannel = new IssueTrackerChannel<>(processorFactory, postProcessor, jobSubTaskAccessor) {};

        MessageResult testResult = issueTrackerChannel.distributeMessages(
            distributionJobDetailsModel,
            ProviderMessageHolder.empty(),
            null,
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            Set.of()
        );

        IssueTrackerResponse<?> processorResponse = processor.processMessages(ProviderMessageHolder.empty(), "jobName");
        assertEquals(processorResponse.getStatusMessage(), testResult.getStatusMessage());
    }

    private JobSubTaskAccessor createJobSubTaskAccessor() {
        return new JobSubTaskAccessor() {
            @Override
            public Optional<JobSubTaskStatusModel> getSubTaskStatus(UUID id) {
                return Optional.empty();
            }

            @Override
            public JobSubTaskStatusModel createSubTaskStatus(UUID id, UUID jobId, Long remainingTaskCount, Set<Long> notificationIds) {
                return null;
            }

            @Override
            public Optional<JobSubTaskStatusModel> updateTaskCount(UUID id, Long remainingTaskCount) {
                return Optional.empty();
            }

            @Override
            public Optional<JobSubTaskStatusModel> decrementTaskCount(UUID id) {
                return Optional.empty();
            }

            @Override
            public Optional<JobSubTaskStatusModel> removeSubTaskStatus(UUID id) {
                return Optional.empty();
            }
        };
    }

    private IssueTrackerAsyncMessageSender<String> createMessageSender(JobSubTaskAccessor jobSubTaskAccessor) {
        IssueTrackerCommentEventGenerator<String> commenter = (model) -> null;
        IssueTrackerTransitionEventGenerator<String> transitioner = (model) -> null;
        IssueTrackerCreationEventGenerator creator = (model) -> null;
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        EventManager eventManager = new EventManager(new Gson(), rabbitTemplate, new SyncTaskExecutor());
        ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);
        return new IssueTrackerAsyncMessageSender<>(
            creator,
            transitioner,
            commenter,
            eventManager,
            jobSubTaskAccessor,
            UUID.randomUUID(),
            UUID.randomUUID(),
            Set.of(1L, 2L, 3L),
            executingJobManager
        );
    }

    private IssueTrackerMessageFormatter createFormatter() {
        return new IssueTrackerMessageFormatter(10000, 10000, 10000, "\n") {
            @Override
            public String encode(String txt) {
                return txt;
            }

            @Override
            public String emphasize(String txt) {
                return txt;
            }

            @Override
            public String createLink(String txt, String url) {
                return url;
            }
        };
    }

}
