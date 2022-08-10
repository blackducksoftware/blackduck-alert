package com.synopsys.integration.alert.api.channel.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.SyncTaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.convert.IssueTrackerMessageFormatter;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCommentEvent;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerCommentEventGenerator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerCreationEventGenerator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueCreator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueTransitioner;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerTransitionEventGenerator;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
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
        IssueTrackerMessageSender<String> messageSender = createMessageSender(jobSubTaskAccessor);
        IssueTrackerProcessor<String> processor = new IssueTrackerProcessor<>(modelExtractor, messageSender);

        IssueTrackerProcessorFactory<DistributionJobDetailsModel, String> processorFactory = (x, y, z) -> processor;

        IssueTrackerResponsePostProcessor postProcessor = new IssueTrackerResponsePostProcessor() {
            @Override
            public <T extends Serializable> void postProcess(IssueTrackerResponse<T> response) {
            }
        };
        IssueTrackerChannel<DistributionJobDetailsModel, String> issueTrackerChannel = new IssueTrackerChannel<>(processorFactory, postProcessor, jobSubTaskAccessor) {};

        MessageResult testResult = issueTrackerChannel.distributeMessages(distributionJobDetailsModel, ProviderMessageHolder.empty(), null, UUID.randomUUID(), Set.of());

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

    private IssueTrackerMessageSender<String> createMessageSender(JobSubTaskAccessor jobSubTaskAccessor) {
        IssueTrackerIssueCommenter<String> commenter = new IssueTrackerIssueCommenter<>(null) {
            @Override
            protected boolean isCommentingEnabled() {
                return false;
            }

            @Override
            protected void addComment(String comment, ExistingIssueDetails<String> existingIssueDetails, @Nullable ProjectIssueModel source) {
            }
        };
        IssueTrackerIssueTransitioner<String> transitioner = new IssueTrackerIssueTransitioner<>(commenter, null) {
            @Override
            protected Optional<String> retrieveJobTransitionName(IssueOperation issueOperation) {
                return Optional.empty();
            }

            @Override
            protected boolean isTransitionRequired(ExistingIssueDetails<String> existingIssueDetails, IssueOperation issueOperation) {
                return false;
            }

            @Override
            protected void findAndPerformTransition(ExistingIssueDetails<String> existingIssueDetails, String transitionName) {
            }
        };
        IssueTrackerIssueCreator<String> creator = new IssueTrackerIssueCreator<>(null, commenter, null) {
            @Override
            protected ExistingIssueDetails<String> createIssueAndExtractDetails(IssueCreationModel alertIssueCreationModel) {
                return null;
            }

            @Override
            protected void assignAlertSearchProperties(ExistingIssueDetails<String> createdIssueDetails, ProjectIssueModel alertIssueSource) {
            }
        };

        IssueTrackerCreationEventGenerator creationEventGenerator = model -> new IssueTrackerCreateIssueEvent("", UUID.randomUUID(), UUID.randomUUID(), Set.of(), null);

        IssueTrackerTransitionEventGenerator<String> transitionEventGenerator = model -> new IssueTrackerTransitionIssueEvent<>(
            "",
            UUID.randomUUID(),
            UUID.randomUUID(),
            Set.of(),
            null
        );

        IssueTrackerCommentEventGenerator<String> commentEventGenerator = model -> new IssueTrackerCommentEvent<>("", UUID.randomUUID(), UUID.randomUUID(), Set.of(), null);

        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        EventManager eventManager = new EventManager(new Gson(), rabbitTemplate, new SyncTaskExecutor());
        return new IssueTrackerMessageSender<>(
            creator,
            transitioner,
            commenter,
            creationEventGenerator,
            transitionEventGenerator,
            commentEventGenerator,
            eventManager,
            jobSubTaskAccessor
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
