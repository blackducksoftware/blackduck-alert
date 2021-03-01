/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.workflow.processor.message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public abstract class MessageContentProcessor {
    private final ProcessingType processingType;

    public MessageContentProcessor(ProcessingType processingType) {
        this.processingType = processingType;
    }

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public abstract List<MessageContentGroup> process(List<ProviderMessageContent> messages);

    public List<MessageContentGroup> createMessageContentGroups(List<ProviderMessageContent> messages) {
        Map<ContentKey, MessageContentGroup> messageGroups = new LinkedHashMap<>();
        messages.stream()
            .filter(this::filterEmptyContent)
            .forEach(message ->
                         messageGroups.computeIfAbsent(message.getContentKey(), ignored -> new MessageContentGroup()).add(message)
            );

        return new ArrayList<>(messageGroups.values());
    }

    protected boolean filterEmptyContent(ProviderMessageContent message) {
        return !message.getComponentItems().isEmpty() || message.isTopLevelActionOnly();
    }

}
