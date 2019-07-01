package com.synopsys.integration.alert.common.workflow.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;

public class DigestMessageContentProcessorTest extends ProcessorTest {

    @Test
    public void processTest() {
        DigestMessageContentProcessor digestMessageContentProcessor = new DigestMessageContentProcessor(new MessageContentCollapser());
        List<AggregateMessageContent> messages = createDefaultMessages();
        final List<MessageContentGroup> messageGroups = digestMessageContentProcessor.process(messages);
        assertFalse(messageGroups.isEmpty());
        assertEquals(2, messageGroups.size());
        messageGroups.forEach(group -> assertEquals(1, group.getSubContent().size()));
        final MessageContentGroup firstGroup = messageGroups.get(0);
        assertEquals("Topic One", firstGroup.getCommonTopic().getName());
        assertEquals(1, firstGroup.getSubContent().get(0).getCategoryItems().size());

        final MessageContentGroup secondGroup = messageGroups.get(1);
        assertEquals("Topic Two", secondGroup.getCommonTopic().getName());
        assertEquals(1, secondGroup.getSubContent().get(0).getCategoryItems().size());
    }
}
