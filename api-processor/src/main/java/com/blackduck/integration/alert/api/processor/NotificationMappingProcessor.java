/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractionDelegator;
import com.blackduck.integration.alert.api.processor.mapping.JobNotificationMapper2;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.logging.AlertLoggerFactory;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;

@Component
public class NotificationMappingProcessor {
    public static final int DEFAULT_BATCH_LIMIT = 10000;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Logger notificationLogger = AlertLoggerFactory.getNotificationLogger(getClass());
    private final NotificationDetailExtractionDelegator notificationDetailExtractionDelegator;
    private final JobNotificationMapper2 jobNotificationMapper;
    private final NotificationAccessor notificationAccessor;

    @Autowired
    public NotificationMappingProcessor(
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator,
        JobNotificationMapper2 jobNotificationMapper,
        NotificationAccessor notificationAccessor
    ) {
        this.notificationDetailExtractionDelegator = notificationDetailExtractionDelegator;
        this.jobNotificationMapper = jobNotificationMapper;
        this.notificationAccessor = notificationAccessor;
    }

    public void processNotifications(UUID correlationID, List<AlertNotificationModel> notifications, List<FrequencyType> frequencies) {
        logNotifications("Start mapping notifications: {}", notifications);
        notificationAccessor.setNotificationsMapping(notifications);
        List<DetailedNotificationContent> filterableNotifications = notifications
            .stream()
            .map(notificationDetailExtractionDelegator::wrapNotification)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        jobNotificationMapper.mapJobsToNotifications(correlationID, filterableNotifications, frequencies);
        notificationAccessor.setNotificationsProcessed(notifications);
        logNotifications("Finished mapping notifications: {}", notifications);
    }

    public boolean hasExceededBatchLimit(UUID correlationID) {
        return jobNotificationMapper.hasBatchReachedSizeLimit(correlationID, DEFAULT_BATCH_LIMIT);
    }

    private void logNotifications(String messageFormat, List<AlertNotificationModel> notifications) {
        if (logger.isDebugEnabled()) {
            List<Long> notificationIds = notifications.stream()
                .map(AlertNotificationModel::getId)
                .collect(Collectors.toList());
            String joinedIds = StringUtils.join(notificationIds, ", ");
            notificationLogger.debug(messageFormat, joinedIds);
        }
    }
}
