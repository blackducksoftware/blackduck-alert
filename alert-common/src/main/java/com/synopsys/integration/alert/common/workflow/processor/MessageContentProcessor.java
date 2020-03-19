/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.workflow.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public abstract class MessageContentProcessor {
    private final FormatType formatType;

    public MessageContentProcessor(final FormatType formatType) {
        this.formatType = formatType;
    }

    public FormatType getFormat() {
        return formatType;
    }

    public abstract List<MessageContentGroup> process(final List<ProviderMessageContent> messages);

    public List<MessageContentGroup> createMessageContentGroups(List<ProviderMessageContent> messages) {
        final Map<ContentKey, MessageContentGroup> messageGroups = new LinkedHashMap<>();
        messages.stream()
            .filter(message -> !message.getComponentItems().isEmpty())
            .forEach(message -> {
                messageGroups.computeIfAbsent(message.getContentKey(), ignored -> new MessageContentGroup()).add(message);
            });

        return new ArrayList<>(messageGroups.values());
    }

}
