/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.SyncTaskExecutor;

import com.blackduck.integration.alert.api.channel.issue.tracker.convert.IssueTrackerMessageFormatter;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.DefaultIssueTrackerEventGenerator;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerAsyncMessageSender;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerCommentEventGenerator;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerCreationEventGenerator;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerTransitionEventGenerator;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

class IssueTrackerChannelTest {
    @Test
    void distributeMessagesTest() throws AlertException {
        UUID jobId = UUID.randomUUID();
        DistributionJobDetailsModel distributionJobDetailsModel = new DistributionJobDetailsModel(ChannelKeys.SLACK, jobId) {
            private static final long serialVersionUID = 5355069038110415471L;
        };
        IssueTrackerModelExtractor<String> modelExtractor = new IssueTrackerModelExtractor<>(createFormatter(), null);
        IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<String>> messageSender = createMessageSender();
        IssueTrackerProcessor<String> processor = new IssueTrackerProcessor<>(modelExtractor, messageSender);

        IssueTrackerProcessorFactory<DistributionJobDetailsModel, String> processorFactory = (x, y, z) -> processor;

        IssueTrackerChannel<DistributionJobDetailsModel, String> issueTrackerChannel = new IssueTrackerChannel<>(processorFactory) {};

        MessageResult testResult = issueTrackerChannel.distributeMessages(
            distributionJobDetailsModel,
            ProviderMessageHolder.empty(),
            null,
            UUID.randomUUID(),
            UUID.randomUUID(),
            Set.of()
        );

        IssueTrackerResponse<?> processorResponse = processor.processMessages(ProviderMessageHolder.empty(), "jobName");
        assertEquals(processorResponse.getStatusMessage(), testResult.getStatusMessage());
    }

    private IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<String>> createMessageSender() {
        IssueTrackerCommentEventGenerator<String> commenter = (model) -> null;
        IssueTrackerTransitionEventGenerator<String> transitioner = (model) -> null;
        IssueTrackerCreationEventGenerator creator = (model) -> null;
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        EventManager eventManager = new EventManager(BlackDuckServicesFactory.createDefaultGson(), rabbitTemplate, new SyncTaskExecutor());
        ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);
        DefaultIssueTrackerEventGenerator<String> eventGenerator = new DefaultIssueTrackerEventGenerator<>(creator, transitioner, commenter);
        return new IssueTrackerAsyncMessageSender<>(
            eventGenerator,
            eventManager,
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
