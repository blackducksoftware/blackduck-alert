/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.detail;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class NotificationDetailExtractionDelegator {
    private final Logger logger = LoggerFactory.getLogger(NotificationDetailExtractionDelegator.class);

    private final EnumMap<NotificationType, NotificationDetailExtractor> notificationExtractors;

    @Autowired
    public NotificationDetailExtractionDelegator(List<NotificationDetailExtractor> notificationDetailExtractors) {
        this.notificationExtractors = initializeExtractorMap(notificationDetailExtractors);
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
            return notificationDetailExtractor.extractDetailedContent(notification);
        }

        logger.warn("Did not find extractor for notification type: {}", notificationTypeString);
        return List.of();
    }

    private Optional<NotificationDetailExtractor> getNotificationExtractor(NotificationType notificationType) {
        NotificationDetailExtractor notificationDetailExtractor = notificationExtractors.get(notificationType);
        return Optional.ofNullable(notificationDetailExtractor);
    }

    private EnumMap<NotificationType, NotificationDetailExtractor> initializeExtractorMap(List<NotificationDetailExtractor> notificationDetailExtractors) {
        return notificationDetailExtractors
                   .stream()
                   .collect(DataStructureUtils.toEnumMap(NotificationDetailExtractor::getNotificationType, NotificationType.class));
    }

}
