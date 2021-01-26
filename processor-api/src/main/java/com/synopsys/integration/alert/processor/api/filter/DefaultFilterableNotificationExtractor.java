/**
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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.filter.extractor.NotificationExtractor;
import com.synopsys.integration.alert.processor.api.filter.model.FilterableNotificationWrapper;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class DefaultFilterableNotificationExtractor implements FilterableNotificationExtractor {
    private final Logger logger = LoggerFactory.getLogger(DefaultFilterableNotificationExtractor.class);
    private List<NotificationExtractor> notificationExtractors;

    @Autowired
    public DefaultFilterableNotificationExtractor(List<NotificationExtractor> notificationExtractors) {
        this.notificationExtractors = notificationExtractors;
    }

    @Override
    public final Optional<FilterableNotificationWrapper<?>> wrapNotification(AlertNotificationModel notification) {
        String notificationTypeString = notification.getNotificationType();
        NotificationType notificationType;
        try {
            notificationType = Enum.valueOf(NotificationType.class, notificationTypeString);
        } catch (IllegalArgumentException e) {
            logger.warn("Notification did not match any supported notification type: {}", notificationTypeString);
            return Optional.empty();
        }

        Optional<NotificationExtractor> notificationExtractorOptional = getNotificationExtractor(notificationType);
        if (notificationExtractorOptional.isPresent()) {
            NotificationExtractor notificationExtractor = notificationExtractorOptional.get();

            String content = notification.getContent();
            FilterableNotificationWrapper filterableNotificationWrapper = notificationExtractor.convertToFilterableNotificationWrapper(content);
            return Optional.of(filterableNotificationWrapper);
        }

        logger.warn("Did not find extractor for notification type: {}", notificationTypeString);
        return Optional.empty();
    }

    private Optional<NotificationExtractor> getNotificationExtractor(NotificationType notificationType) {
        return notificationExtractors.stream()
                   .filter(notificationExtractor -> notificationExtractor.getNotificationType() == notificationType)
                   .findFirst();
    }
}
