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
import com.synopsys.integration.alert.processor.api.filter.extractor.NotificationDetailExtractor;
import com.synopsys.integration.alert.processor.api.filter.model.DetailedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class NotificationDetailExtractionDelegator {
    private final Logger logger = LoggerFactory.getLogger(NotificationDetailExtractionDelegator.class);

    private final Map<NotificationType, NotificationDetailExtractor> notificationExtractors;

    @Autowired
    public NotificationDetailExtractionDelegator(List<NotificationDetailExtractor> notificationDetailExtractors) {
        this.notificationExtractors = DataStructureUtils.mapToValues(notificationDetailExtractors, NotificationDetailExtractor::getNotificationType);
    }

    public final List<DetailedNotificationContent> wrapNotification(AlertNotificationModel notification) {
        String notificationTypeString = notification.getNotificationType();
        NotificationType notificationType;
        try {
            notificationType = Enum.valueOf(NotificationType.class, notificationTypeString);
        } catch (IllegalArgumentException e) {
            logger.warn("Notification did not match any existing notification type: {}", notificationTypeString);
            return List.of();
        }

        Optional<NotificationDetailExtractor> notificationExtractorOptional = getNotificationExtractor(notificationType);
        if (notificationExtractorOptional.isPresent()) {
            NotificationDetailExtractor notificationDetailExtractor = notificationExtractorOptional.get();
            return notificationDetailExtractor.convertToFilterableNotificationWrapper(notification);
        }

        logger.warn("Did not find extractor for notification type: {}", notificationTypeString);
        return List.of();
    }

    private Optional<NotificationDetailExtractor> getNotificationExtractor(NotificationType notificationType) {
        NotificationDetailExtractor notificationDetailExtractor = notificationExtractors.get(notificationType);
        return Optional.ofNullable(notificationDetailExtractor);
    }

}
