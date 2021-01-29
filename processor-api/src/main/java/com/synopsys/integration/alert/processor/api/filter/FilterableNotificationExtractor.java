/*
 * processor-api
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
package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.processor.api.filter.extractor.NotificationExtractor;
import com.synopsys.integration.alert.processor.api.filter.model.FilterableNotificationWrapper;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class FilterableNotificationExtractor {
    private final Logger logger = LoggerFactory.getLogger(FilterableNotificationExtractor.class);

    private final Map<NotificationType, NotificationExtractor> notificationExtractors;

    @Autowired
    public FilterableNotificationExtractor(List<NotificationExtractor> notificationExtractors) {
        this.notificationExtractors = DataStructureUtils.mapToValues(notificationExtractors, NotificationExtractor::getNotificationType);
    }

    public final List<FilterableNotificationWrapper> wrapNotification(AlertNotificationModel notification) {
        String notificationTypeString = notification.getNotificationType();
        NotificationType notificationType;
        try {
            notificationType = Enum.valueOf(NotificationType.class, notificationTypeString);
        } catch (IllegalArgumentException e) {
            logger.warn("Notification did not match any existing notification type: {}", notificationTypeString);
            return List.of();
        }

        Optional<NotificationExtractor> notificationExtractorOptional = getNotificationExtractor(notificationType);
        if (notificationExtractorOptional.isPresent()) {
            NotificationExtractor notificationExtractor = notificationExtractorOptional.get();

            return notificationExtractor.convertToFilterableNotificationWrapper(notification);
        }

        logger.warn("Did not find extractor for notification type: {}", notificationTypeString);
        return List.of();
    }

    private Optional<NotificationExtractor> getNotificationExtractor(NotificationType notificationType) {
        NotificationExtractor notificationExtractor = notificationExtractors.get(notificationType);
        return Optional.ofNullable(notificationExtractor);
    }

}
