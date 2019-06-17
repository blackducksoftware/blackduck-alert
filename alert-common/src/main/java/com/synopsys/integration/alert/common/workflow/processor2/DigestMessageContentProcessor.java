package com.synopsys.integration.alert.common.workflow.processor2;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.message.model2.MessageContentGroup;

@Component
public class DigestMessageContentProcessor extends MessageContentProcessor {
    private final DefaultMessageContentProcessor defaultMessageContentProcessor;
    private final MessageContentCollapser messageContentCollapser;

    @Autowired
    public DigestMessageContentProcessor(final DefaultMessageContentProcessor defaultMessageContentProcessor, final MessageContentCollapser messageContentCollapser) {
        super(FormatType.DIGEST);
        this.defaultMessageContentProcessor = defaultMessageContentProcessor;
        this.messageContentCollapser = messageContentCollapser;
    }

    @Override
    public List<MessageContentGroup> process(final List<ProviderMessageContent> messages) {
        final List<ProviderMessageContent> collapsedMessages = messageContentCollapser.collapse(messages);
        return defaultMessageContentProcessor.process(collapsedMessages);
    }

}
