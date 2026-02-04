/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.processing;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.blackduck.integration.alert.api.event.AlertEventHandler;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.event.NotificationReceivedEvent;
import com.blackduck.integration.alert.api.processor.NotificationMappingProcessor;
import com.blackduck.integration.alert.api.processor.event.JobNotificationMappedEvent;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

@Component
public class NotificationReceivedEventHandler implements AlertEventHandler<NotificationReceivedEvent> {
    public static final int PAGE_SIZE = 200;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NotificationAccessor notificationAccessor;
    private final NotificationMappingProcessor notificationMappingProcessor;
    private final EventManager eventManager;

    @Autowired
    public NotificationReceivedEventHandler(
        NotificationAccessor notificationAccessor,
        NotificationMappingProcessor notificationMappingProcessor,
        EventManager eventManager
    ) {
        this.notificationAccessor = notificationAccessor;
        this.notificationMappingProcessor = notificationMappingProcessor;
        this.eventManager = eventManager;
    }

    @Override
    public void handle(NotificationReceivedEvent event) {
        logger.debug("Event {}", event);
        logger.info("Processing event {} for notifications.", event.getEventId());
        processNotifications(event);
        logger.info("Finished processing event {} for notifications.", event.getEventId());
    }

    private void processNotifications(NotificationReceivedEvent event) {
        UUID correlationID = event.getCorrelationId();
        long providerConfigId = event.getProviderConfigId();
        UUID accumulationBatchId = event.getBatchId();
        AlertPagedModel<AlertNotificationModel> pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotMapped(providerConfigId, PAGE_SIZE);
        if (!CollectionUtils.isEmpty(pageOfAlertNotificationModels.getModels())) {
            List<AlertNotificationModel> notifications = pageOfAlertNotificationModels.getModels();
            logger.info("Starting to process batch for provider({}): {} = {} notifications.", providerConfigId, correlationID, notifications.size());
            notificationMappingProcessor.processNotifications(correlationID, notifications, List.of(FrequencyType.REAL_TIME));
            boolean hasMoreNotificationsToMap = notificationAccessor.hasMoreNotificationsToMap(providerConfigId);
            if (hasMoreNotificationsToMap) {
                NotificationReceivedEvent continueProcessingEvent;
                if (notificationMappingProcessor.hasExceededBatchLimit(correlationID)) {
                    logger.info("Mapping batch limit of {} exceeded for correlation id: {}. Continuing processing in next batch.", notificationMappingProcessor.getNotificationMappingBatchLimit(), correlationID);
                    eventManager.sendEvent(new JobNotificationMappedEvent(correlationID));
                    continueProcessingEvent = new NotificationReceivedEvent(providerConfigId, accumulationBatchId);
                } else {
                    continueProcessingEvent = new NotificationReceivedEvent(correlationID, providerConfigId, accumulationBatchId);
                }
                eventManager.sendEvent(continueProcessingEvent);
            } else {
                eventManager.sendEvent(new JobNotificationMappedEvent(correlationID));
            }
        }
        logger.info("Finished processing batch for provider({}): {} event for notifications.", providerConfigId, correlationID);
    }
}
