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

import java.util.List;
import java.util.Map;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.alert.common.workflow.processor.message.MessageContentProcessor;

public abstract class ProviderMessageContentCollector {
    private final Map<ProcessingType, MessageContentProcessor> messageContentProcessorMap;

    public ProviderMessageContentCollector(List<MessageContentProcessor> messageContentFormatters) {
        this.messageContentProcessorMap = DataStructureUtils.mapToValues(messageContentFormatters, MessageContentProcessor::getProcessingType);
    }

    public final List<MessageContentGroup> createMessageContentGroups(DistributionJobModel job, NotificationDeserializationCache cache, List<AlertNotificationModel> notifications) throws AlertException {
        List<ProviderMessageContent> messages = createProviderMessageContents(job, cache, notifications);
        return messageContentProcessorMap.get(job.getProcessingType()).process(messages);
    }

    protected abstract List<ProviderMessageContent> createProviderMessageContents(DistributionJobModel job, NotificationDeserializationCache cache, List<AlertNotificationModel> notifications) throws AlertException;

}
