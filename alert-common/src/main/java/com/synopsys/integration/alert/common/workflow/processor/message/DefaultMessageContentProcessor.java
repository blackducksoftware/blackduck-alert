/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.workflow.processor.message;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.workflow.combiner.AbstractMessageCombiner;
import com.synopsys.integration.alert.common.workflow.combiner.DefaultMessageCombiner;

@Component
public class DefaultMessageContentProcessor extends MessageContentProcessor {
    private final AbstractMessageCombiner messageCombiner;

    @Autowired
    public DefaultMessageContentProcessor(DefaultMessageCombiner messageCombiner) {
        super(ProcessingType.DEFAULT);
        this.messageCombiner = messageCombiner;
    }

    @Override
    public List<MessageContentGroup> process(List<ProviderMessageContent> messages) {
        List<ProviderMessageContent> combinedMessages = messageCombiner.combine(messages);
        return createMessageContentGroups(combinedMessages);
    }

}
