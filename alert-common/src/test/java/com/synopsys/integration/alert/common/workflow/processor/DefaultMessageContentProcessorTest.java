package com.synopsys.integration.alert.common.workflow.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.workflow.combiner.DefaultMessageCombiner;
import com.synopsys.integration.alert.common.workflow.processor.message.DefaultMessageContentProcessor;

public class DefaultMessageContentProcessorTest extends ProcessorTest {
    @Test
    public void processTest() throws AlertException {
        DefaultMessageContentProcessor defaultMessageContentProcessor = new DefaultMessageContentProcessor(new DefaultMessageCombiner());
        List<ProviderMessageContent> messages = createDefaultMessages();

        List<MessageContentGroup> messageGroups = defaultMessageContentProcessor.process(messages);
        assertFalse(messageGroups.isEmpty());
        assertEquals(2, messageGroups.size());
        messageGroups.forEach(group -> assertEquals(1, group.getSubContent().size()));

        MessageContentGroup firstGroup = messageGroups.get(0);
        assertEquals("Topic One", firstGroup.getCommonTopic().getLabel());
        assertEquals(4, firstGroup.getSubContent().get(0).getComponentItems().size());

        MessageContentGroup secondGroup = messageGroups.get(1);
        assertEquals("Topic Two", secondGroup.getCommonTopic().getLabel());
        assertEquals(1, secondGroup.getSubContent().get(0).getComponentItems().size());
    }

}
