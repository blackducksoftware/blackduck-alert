package com.synopsys.integration.alert.api.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.convert.AbstractChannelMessageConverter;
import com.synopsys.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

class MessageBoardChannelTest {
    @Test
    void distributeMessagesTest() throws AlertException {
        MessageResult expectedResult = new MessageResult("Test result");
        EventManager eventManager = Mockito.mock(EventManager.class);
        DistributionJobDetailsModel testDetails = new DistributionJobDetailsModel(null, null) {};

        AbstractChannelMessageConverter<DistributionJobDetailsModel, Object> converter = createConverter();
        ChannelMessageSender<DistributionJobDetailsModel, Object, MessageResult> sender = (x, y) -> expectedResult;
        MessageBoardChannel<DistributionJobDetailsModel, Object> messageBoardChannel = new MessageBoardChannel<>(converter, sender, eventManager) {};

        MessageResult testResult = messageBoardChannel.distributeMessages(testDetails, ProviderMessageHolder.empty(), "jobName", UUID.randomUUID(), UUID.randomUUID(), Set.of());
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
