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
import com.synopsys.integration.alert.common.workflow.combiner.MessageOperationCombiner;
import com.synopsys.integration.alert.common.workflow.combiner.TopLevelActionCombiner;

@Component
public class DigestMessageContentProcessor extends MessageContentProcessor {
    private final TopLevelActionCombiner topLevelActionCombiner;
    private final MessageOperationCombiner messageOperationCombiner;

    @Autowired
    public DigestMessageContentProcessor(TopLevelActionCombiner topLevelActionCombiner, MessageOperationCombiner messageOperationCombiner) {
        super(ProcessingType.DIGEST);
        this.topLevelActionCombiner = topLevelActionCombiner;
        this.messageOperationCombiner = messageOperationCombiner;
    }

    @Override
    public List<MessageContentGroup> process(List<ProviderMessageContent> messages) {
        List<ProviderMessageContent> messagesCombinedAtTopLevel = topLevelActionCombiner.combine(messages);
        List<ProviderMessageContent> messagesCombinedAtComponentLevel = messageOperationCombiner.combine(messagesCombinedAtTopLevel);
        return createMessageContentGroups(messagesCombinedAtComponentLevel);
    }

}
