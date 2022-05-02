/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processing;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.processor.api.NotificationProcessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessor2;
import com.synopsys.integration.alert.processor.api.event.JobNotificationMappedEvent;

@Component
public class NotificationReceivedEventHandler implements AlertEventHandler<NotificationReceivedEvent> {
    private static final int PAGE_SIZE = 100;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NotificationAccessor notificationAccessor;
    private final NotificationProcessor notificationProcessor;
    private final NotificationProcessor2 notificationProcessor2;
    private final EventManager eventManager;

    @Autowired
    public NotificationReceivedEventHandler(
        NotificationAccessor notificationAccessor,
        NotificationProcessor notificationProcessor,
        NotificationProcessor2 notificationProcessor2,
        EventManager eventManager
    ) {
        this.notificationAccessor = notificationAccessor;
        this.notificationProcessor = notificationProcessor;
        this.notificationProcessor2 = notificationProcessor2;
        this.eventManager = eventManager;
    }

    @Override
    public void handle(NotificationReceivedEvent event) {
        logger.debug("Event {}", event);
        logger.info("Processing event {} for notifications.", event.getEventId());
        logger.info("Processing event for notifications.");
        processNotifications(event.getCorrelationId());
        logger.info("Finished processing event {} for notifications.", event.getEventId());
    }

    private void processNotifications(UUID correlationID) {
        AlertPagedModel<AlertNotificationModel> pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(PAGE_SIZE);
        if (!CollectionUtils.isEmpty(pageOfAlertNotificationModels.getModels())) {
            List<AlertNotificationModel> notifications = pageOfAlertNotificationModels.getModels();
            logger.info("Starting to process {} notifications.", notifications.size());
            //notificationProcessor.processNotifications(notifications, List.of(FrequencyType.REAL_TIME));
            notificationProcessor2.processNotifications(correlationID, notifications, List.of(FrequencyType.REAL_TIME));
            boolean hasMoreNotificationsToProcess = notificationAccessor.hasMoreNotificationsToProcess();
            if (hasMoreNotificationsToProcess) {
                eventManager.sendEvent(new NotificationReceivedEvent(correlationID));
            } else {
                eventManager.sendEvent(new JobNotificationMappedEvent(correlationID));
            }
        }
        logger.info("Finished processing event for notifications.");
    }
}
