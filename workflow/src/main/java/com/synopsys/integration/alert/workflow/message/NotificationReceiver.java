/*
 * workflow
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.workflow.message;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.AlertDefaultEventListener;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.alert.processor.api.NotificationProcessor;

@Component(value = NotificationReceiver.COMPONENT_NAME)
public class NotificationReceiver extends MessageReceiver<NotificationReceivedEvent> implements AlertDefaultEventListener {
    public static final String COMPONENT_NAME = "notification_receiver";
    private static final int PAGE_SIZE = 100;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NotificationAccessor notificationAccessor;
    private final NotificationProcessor notificationProcessor;
    private final EventManager eventManager;

    @Autowired
    public NotificationReceiver(Gson gson, NotificationAccessor notificationAccessor, NotificationProcessor notificationProcessor, EventManager eventManager) {
        super(gson, NotificationReceivedEvent.class);
        this.notificationAccessor = notificationAccessor;
        this.notificationProcessor = notificationProcessor;
        this.eventManager = eventManager;
    }

    @Override
    public void handleEvent(NotificationReceivedEvent event) {
        logger.debug("Event {}", event);
        logger.info("Processing event for notifications.");

        AlertPagedModel<AlertNotificationModel> pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(PAGE_SIZE);
        if (!CollectionUtils.isEmpty(pageOfAlertNotificationModels.getModels())) {
            List<AlertNotificationModel> notifications = pageOfAlertNotificationModels.getModels();
            logger.info("Starting to process {} notifications.", notifications.size());
            notificationProcessor.processNotifications(notifications, List.of(FrequencyType.REAL_TIME));
            pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(PAGE_SIZE);
            if (!CollectionUtils.isEmpty(pageOfAlertNotificationModels.getModels())) {
                eventManager.sendEvent(new NotificationReceivedEvent());
            }
        }
        logger.info("Finished processing event for notifications.");
    }

    @Override
    public String getDestinationName() {
        return NotificationReceivedEvent.NOTIFICATION_RECEIVED_EVENT_TYPE;
    }

}
