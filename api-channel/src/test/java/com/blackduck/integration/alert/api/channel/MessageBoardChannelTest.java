/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.convert.AbstractChannelMessageConverter;
import com.blackduck.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

class MessageBoardChannelTest {
    @Test
    void distributeMessagesTest() throws AlertException {
        MessageResult expectedResult = new MessageResult("Test result");
        EventManager eventManager = Mockito.mock(EventManager.class);
        ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);
        DistributionJobDetailsModel testDetails = new DistributionJobDetailsModel(null, null) {};

        AbstractChannelMessageConverter<DistributionJobDetailsModel, Object> converter = createConverter();
        ChannelMessageSender<DistributionJobDetailsModel, Object, MessageResult> sender = (x, y) -> expectedResult;
        MessageBoardChannel<DistributionJobDetailsModel, Object> messageBoardChannel = new MessageBoardChannel<>(converter, sender, eventManager, executingJobManager) {};

        MessageResult testResult = messageBoardChannel.distributeMessages(
            testDetails,
            ProviderMessageHolder.empty(),
            "jobName",
            UUID.randomUUID(),
            UUID.randomUUID(),
            Set.of()
        );
        assertEquals(expectedResult, testResult);
    }

    private AbstractChannelMessageConverter<DistributionJobDetailsModel, Object> createConverter() {
        ChannelMessageFormatter formatter = createFormatter();
        return new AbstractChannelMessageConverter<>(formatter) {
            @Override
            protected List<Object> convertSimpleMessageToChannelMessages(DistributionJobDetailsModel distributionDetails, SimpleMessage simpleMessage, List<String> messageChunks) {
                return List.of();
            }

            @Override
            protected List<Object> convertProjectMessageToChannelMessages(DistributionJobDetailsModel distributionDetails, ProjectMessage projectMessage, List<String> messageChunks) {
                return List.of();
            }
        };
    }

    private ChannelMessageFormatter createFormatter() {
        return new ChannelMessageFormatter(10, "\n") {
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
                return txt;
            }
        };
    }

}
