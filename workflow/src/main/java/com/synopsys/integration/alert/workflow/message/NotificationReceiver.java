/*
 * workflow
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.workflow.message;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.ChannelEventManager;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.AlertDefaultEventListener;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.alert.common.workflow.processor.notification.NotificationProcessor;

public class NotificationReceiver extends MessageReceiver<NotificationReceivedEvent> implements AlertDefaultEventListener {
    private final static int MAX_NUMBER_PAGES_PROCESSED = 100;
    public static final String COMPONENT_NAME = "notification_receiver";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final NotificationAccessor notificationAccessor;
    private final NotificationProcessor notificationProcessor;
    private final ChannelEventManager eventManager;

    public NotificationReceiver(Gson gson, NotificationAccessor notificationAccessor, NotificationProcessor notificationProcessor, ChannelEventManager eventManager) {
        super(gson, NotificationReceivedEvent.class);
        this.notificationAccessor = notificationAccessor;
        this.notificationProcessor = notificationProcessor;
        this.eventManager = eventManager;
    }

    @Override
    public void handleEvent(NotificationReceivedEvent event) {
        logger.debug("Event {}", event);
        logger.info("Processing event for notifications.");

        int numPagesProcessed = 0;
        int pageSize = 100;

        //TODO: Addition in 6.4.0 to set a 2 hour timeout, this should be removed once we properly handle sending messages to channels
        OffsetDateTime timeLimit = DateUtils.createCurrentDateTimestamp().plusHours(2);
        boolean hasTimedOut = false;

        AlertPagedModel<AlertNotificationModel> pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(pageSize);
        //TODO: Once we create a way of handling channel events in parallel, we can remove the MAX_NUMBER_PAGES_PROCESSED.
        while (!CollectionUtils.isEmpty(pageOfAlertNotificationModels.getModels()) && numPagesProcessed < MAX_NUMBER_PAGES_PROCESSED && !hasTimedOut) {
            List<AlertNotificationModel> notifications = pageOfAlertNotificationModels.getModels();
            logger.info("Sending {} notifications.", notifications.size());
            List<DistributionEvent> distributionEvents = notificationProcessor.processNotifications(FrequencyType.REAL_TIME, notifications);
            logger.info("Sending {} events for notifications.", distributionEvents.size());
            eventManager.sendEvents(distributionEvents);
            notificationAccessor.setNotificationsProcessed(notifications);
            numPagesProcessed++;
            pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(pageSize);
            logger.trace("Processing Page: {}. New pages found: {}",
                numPagesProcessed,
                pageOfAlertNotificationModels.getTotalPages());
            hasTimedOut = DateUtils.createCurrentDateTimestamp().isAfter(timeLimit);
        }
        if (numPagesProcessed == MAX_NUMBER_PAGES_PROCESSED) {
            logger.warn("Receiver reached upper page limit of pages processed: {}, exiting.", MAX_NUMBER_PAGES_PROCESSED);
        }
        //TODO: this check should be removed once we properly handle sending messages to channels
        if (hasTimedOut) {
            logger.warn("Receiver has timed out after 2 hours.");
        }
        logger.info("Finished processing event for notifications.");
    }

    @Override
    public String getDestinationName() {
        return NotificationReceivedEvent.NOTIFICATION_RECEIVED_EVENT_TYPE;
    }

}
