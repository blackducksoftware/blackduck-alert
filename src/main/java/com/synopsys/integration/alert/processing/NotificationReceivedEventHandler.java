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
import com.synopsys.integration.alert.processor.api.NotificationMappingProcessor;
import com.synopsys.integration.alert.processor.api.event.JobNotificationMappedEvent;

@Component
public class NotificationReceivedEventHandler implements AlertEventHandler<NotificationReceivedEvent> {
    private static final int PAGE_SIZE = 200;

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
        AlertPagedModel<AlertNotificationModel> pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(providerConfigId, PAGE_SIZE);
        if (!CollectionUtils.isEmpty(pageOfAlertNotificationModels.getModels())) {
            List<AlertNotificationModel> notifications = pageOfAlertNotificationModels.getModels();
            logger.info("Starting to process batch for provider({}): {} = {} notifications.", providerConfigId, correlationID, notifications.size());
            notificationMappingProcessor.processNotifications(correlationID, notifications, List.of(FrequencyType.REAL_TIME));
            boolean hasMoreNotificationsToProcess = notificationAccessor.hasMoreNotificationsToProcess(providerConfigId);
            if (hasMoreNotificationsToProcess) {
                eventManager.sendEvent(new NotificationReceivedEvent(correlationID, providerConfigId));
            } else {
                eventManager.sendEvent(new JobNotificationMappedEvent(correlationID));
            }
        }
        logger.info("Finished processing batch for provider({}): {} event for notifications.", providerConfigId, correlationID);
    }
}
