/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.channel.event.NotificationToChannelEventConverter;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

@Component
public class NotificationProcessor {
    private final MessageContentAggregator messageContentAggregator;
    private final NotificationToChannelEventConverter notificationToEventConverter;

    @Autowired
    public NotificationProcessor(final MessageContentAggregator messageContentAggregator, final NotificationToChannelEventConverter notificationToEventConverter) {
        this.messageContentAggregator = messageContentAggregator;
        this.notificationToEventConverter = notificationToEventConverter;
    }

    public List<DistributionEvent> processNotifications(final FrequencyType frequencyType, final List<NotificationContent> notificationList) {
        final Map<? extends CommonDistributionConfig, List<AggregateMessageContent>> messageContentList = messageContentAggregator.processNotifications(frequencyType, notificationList);
        final List<DistributionEvent> notificationEvents = notificationToEventConverter.convertToEvents(messageContentList);
        return notificationEvents;
    }

    public List<DistributionEvent> processNotifications(final CommonDistributionConfig commonDistributionConfig, final List<NotificationContent> notificationList) {
        final Map<? extends CommonDistributionConfig, List<AggregateMessageContent>> messageContentList = messageContentAggregator.processNotifications(Arrays.asList(commonDistributionConfig), notificationList);
        final List<DistributionEvent> notificationEvents = notificationToEventConverter.convertToEvents(messageContentList);
        return notificationEvents;
    }
}
