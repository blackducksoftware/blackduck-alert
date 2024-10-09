/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.detail;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.blackduck.api.manual.view.NotificationView;
import com.blackduck.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;

@Component
public class NotificationDetailExtractionDelegator {
    private final Logger logger = LoggerFactory.getLogger(NotificationDetailExtractionDelegator.class);

    private final BlackDuckResponseResolver blackDuckResponseResolver;
    private final Map<Class<? extends NotificationView>, NotificationDetailExtractor> notificationExtractors;

    @Autowired
    public NotificationDetailExtractionDelegator(BlackDuckResponseResolver blackDuckResponseResolver, List<NotificationDetailExtractor> notificationDetailExtractors) {
        this.blackDuckResponseResolver = blackDuckResponseResolver;
        this.notificationExtractors =
            notificationDetailExtractors
                .stream()
                .collect(Collectors.toMap(NotificationDetailExtractor::getNotificationViewClass, Function.identity()));
    }

    public final List<DetailedNotificationContent> wrapNotification(AlertNotificationModel notification) {
        NotificationView notificationView = blackDuckResponseResolver.resolve(notification.getContent(), NotificationView.class);

        Optional<NotificationDetailExtractor> notificationExtractorOptional = getNotificationExtractor(notificationView.getClass());
        if (notificationExtractorOptional.isPresent()) {
            NotificationDetailExtractor notificationDetailExtractor = notificationExtractorOptional.get();
            return notificationDetailExtractor.extractDetailedContent(notification, notificationView);
        }

        logger.warn("Did not find extractor for notification view: {}", notificationView.getClass().getSimpleName());
        return List.of();
    }

    private Optional<NotificationDetailExtractor> getNotificationExtractor(Class<? extends NotificationView> notificationViewClass) {
        NotificationDetailExtractor notificationDetailExtractor = notificationExtractors.get(notificationViewClass);
        return Optional.ofNullable(notificationDetailExtractor);
    }

}
