/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.workflow;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;

public abstract class ProviderMessageContentCollector {
    private Map<FormatType, MessageContentProcessor> messageContentProcessorMap;

    public ProviderMessageContentCollector(List<MessageContentProcessor> messageContentProcessors) {
        this.messageContentProcessorMap = initializeProcessorMap(messageContentProcessors);
    }

    public final List<MessageContentGroup> createMessageContentGroups(ConfigurationJobModel job, NotificationDeserializationCache cache, List<AlertNotificationWrapper> notifications) throws AlertException {
        List<ProviderMessageContent> messages = createProviderMessageContents(job, cache, notifications);
        return messageContentProcessorMap.get(job.getFormatType()).process(messages);
    }

    protected abstract List<ProviderMessageContent> createProviderMessageContents(ConfigurationJobModel job, NotificationDeserializationCache cache, List<AlertNotificationWrapper> notifications) throws AlertException;

    private Map<FormatType, MessageContentProcessor> initializeProcessorMap(List<MessageContentProcessor> messageContentProcessors) {
        return messageContentProcessors
                   .stream()
                   .collect(Collectors.toMap(MessageContentProcessor::getFormat, Function.identity()));
    }

}
