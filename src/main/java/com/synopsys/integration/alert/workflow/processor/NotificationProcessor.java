/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.event.NotificationToDistributionEventConverter;
import com.synopsys.integration.alert.common.data.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.data.model.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;

@Component
public class NotificationProcessor {
    private final MessageContentAggregator messageContentAggregator;
    private final NotificationToDistributionEventConverter notificationToEventConverter;

    @Autowired
    public NotificationProcessor(final MessageContentAggregator messageContentAggregator, final NotificationToDistributionEventConverter notificationToEventConverter) {
        this.messageContentAggregator = messageContentAggregator;
        this.notificationToEventConverter = notificationToEventConverter;
    }

    public List<DistributionEvent> processNotifications(final List<AlertNotificationWrapper> notificationList) {
        final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> messageContentList = messageContentAggregator.processNotifications(notificationList);
        return notificationToEventConverter.convertToEvents(messageContentList);
    }

    public List<DistributionEvent> processNotifications(final FrequencyType frequencyType, final List<AlertNotificationWrapper> notificationList) {
        final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> messageContentList = messageContentAggregator.processNotifications(frequencyType, notificationList);
        return notificationToEventConverter.convertToEvents(messageContentList);
    }

    public List<DistributionEvent> processNotifications(final CommonDistributionConfiguration commonDistributionConfig, final List<AlertNotificationWrapper> notificationList) {
        final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> messageContentList = messageContentAggregator.processNotifications(List.of(commonDistributionConfig), notificationList);
        return notificationToEventConverter.convertToEvents(messageContentList);
    }
}
