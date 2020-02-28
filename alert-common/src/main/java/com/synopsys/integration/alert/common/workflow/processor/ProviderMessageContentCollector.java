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

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.alert.common.workflow.formatter.MessageContentFormatter;

public abstract class ProviderMessageContentCollector {
    private Map<FormatType, MessageContentFormatter> messageContentFormatterMap;

    public ProviderMessageContentCollector(List<MessageContentFormatter> messageContentFormatters) {
        this.messageContentFormatterMap = DataStructureUtils.mapToValues(messageContentFormatters, MessageContentFormatter::getFormat);
    }

    public final List<MessageContentGroup> createMessageContentGroups(ConfigurationJobModel job, NotificationDeserializationCache cache, List<AlertNotificationModel> notifications) throws AlertException {
        List<ProviderMessageContent> messages = createProviderMessageContents(job, cache, notifications);
        return messageContentFormatterMap.get(job.getFormatType()).format(messages);
    }

    protected abstract List<ProviderMessageContent> createProviderMessageContents(ConfigurationJobModel job, NotificationDeserializationCache cache, List<AlertNotificationModel> notifications) throws AlertException;

}
