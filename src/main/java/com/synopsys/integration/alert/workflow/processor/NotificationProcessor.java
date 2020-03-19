/**
 * blackduck-alert
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
package com.synopsys.integration.alert.workflow.processor;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.util.NotificationToDistributionEventConverter;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;

@Component
public class NotificationProcessor {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageContentAggregator messageContentAggregator;
    private final NotificationToDistributionEventConverter notificationToEventConverter;

    @Autowired
    public NotificationProcessor(final MessageContentAggregator messageContentAggregator, final NotificationToDistributionEventConverter notificationToEventConverter) {
        this.messageContentAggregator = messageContentAggregator;
        this.notificationToEventConverter = notificationToEventConverter;
    }

    public List<DistributionEvent> processNotifications(final List<AlertNotificationWrapper> notificationList) {
        final Map<ConfigurationJobModel, List<MessageContentGroup>> messageContentList = messageContentAggregator.processNotifications(notificationList);
        return notificationToEventConverter.convertToEvents(messageContentList);
    }

    public List<DistributionEvent> processNotifications(final FrequencyType frequencyType, final List<AlertNotificationWrapper> notificationList) {
        logger.info("Notifications to Process: {}", notificationList.size());
        if (notificationList.isEmpty()) {
            return List.of();
        }
        final Map<ConfigurationJobModel, List<MessageContentGroup>> messageContentList = messageContentAggregator.processNotifications(frequencyType, notificationList);
        return notificationToEventConverter.convertToEvents(messageContentList);

    }

    public List<DistributionEvent> processNotifications(final ConfigurationJobModel commonDistributionConfig, final List<AlertNotificationWrapper> notificationList) {
        final Map<ConfigurationJobModel, List<MessageContentGroup>> messageContentList = messageContentAggregator.processNotifications(List.of(commonDistributionConfig), notificationList);
        return notificationToEventConverter.convertToEvents(messageContentList);
    }

}
