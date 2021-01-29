/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
