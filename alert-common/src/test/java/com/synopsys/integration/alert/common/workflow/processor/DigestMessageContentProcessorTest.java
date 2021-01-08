package com.synopsys.integration.alert.common.workflow.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.workflow.combiner.MessageOperationCombiner;
import com.synopsys.integration.alert.common.workflow.combiner.TopLevelActionCombiner;
import com.synopsys.integration.alert.common.workflow.processor.message.DigestMessageContentProcessor;

public class DigestMessageContentProcessorTest extends ProcessorTest {
    @Test
    public void processTest() throws AlertException {
        DigestMessageContentProcessor digestMessageContentProcessor = new DigestMessageContentProcessor(new TopLevelActionCombiner(), new MessageOperationCombiner());
        List<ProviderMessageContent> messages = createDefaultMessages();

        List<MessageContentGroup> messageGroups = digestMessageContentProcessor.process(messages);
        assertFalse(messageGroups.isEmpty());
        assertEquals(2, messageGroups.size());
        messageGroups.forEach(group -> assertEquals(1, group.getSubContent().size()));

        MessageContentGroup firstGroup = messageGroups.get(0);
        assertEquals("Topic One", firstGroup.getCommonProject().getName());
        assertEquals(1, firstGroup.getSubContent().get(0).getComponentItems().size());

        MessageContentGroup secondGroup = messageGroups.get(1);
        assertEquals("Topic Two", secondGroup.getCommonProject().getName());
        assertEquals(1, secondGroup.getSubContent().get(0).getComponentItems().size());
    }

}
