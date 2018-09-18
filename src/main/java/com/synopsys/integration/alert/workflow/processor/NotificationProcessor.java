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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.channel.event.NotificationToChannelEventConverter;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.workflow.filter.NotificationFilter;

@Component
public class NotificationProcessor {
    private final MessageContentAggregator messageContentAggregator;
    private final NotificationFilter notificationFilter;
    private final NotificationToChannelEventConverter notificationToEventConverter;

    @Autowired
    public NotificationProcessor(final NotificationFilter notificationFilter, final NotificationToChannelEventConverter notificationToEventConverter, final MessageContentAggregator messageContentAggregator) {
        this.notificationFilter = notificationFilter;
        this.notificationToEventConverter = notificationToEventConverter;
        this.messageContentAggregator = messageContentAggregator;
    }

    public List<ChannelEvent> processNotifications(final FrequencyType frequencyType, final List<NotificationContent> notificationList) {
        final Collection<NotificationContent> filteredNotifications = notificationFilter.extractApplicableNotifications(frequencyType, notificationList);
        // TODO convert notification content to topic contents.  Provider will be responsible for this.
        // TODO only collapse if the process is of type DIGEST.
        final Map<CommonDistributionConfig, List<AggregateMessageContent>> topicContentList = messageContentAggregator.processNotifications(frequencyType, notificationList);

        final List<ChannelEvent> notificationEvents = notificationToEventConverter.convertToEvents(filteredNotifications);
        return notificationEvents;
    }
}
