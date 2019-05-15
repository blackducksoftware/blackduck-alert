package com.synopsys.integration.alert.workflow.processor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.event.NotificationEvent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;

@Component
public class NotificationReceiver extends MessageReceiver<NotificationEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final NotificationManager notificationManager;
    private final NotificationProcessor notificationProcessor;
    private final EventManager eventManager;

    @Autowired
    public NotificationReceiver(final Gson gson, final NotificationManager notificationManager, final NotificationProcessor notificationProcessor, final EventManager eventManager) {
        super(gson, NotificationEvent.class);
        this.notificationManager = notificationManager;
        this.notificationProcessor = notificationProcessor;
        this.eventManager = eventManager;
    }

    @Override
    public void handleEvent(final NotificationEvent event) {
        if (event.getDestination().equals(NotificationEvent.NOTIFICATION_EVENT_TYPE)) {
            if (null == event.getNotificationIds() || event.getNotificationIds().isEmpty()) {
                logger.warn("Can not process a notification event without notification Id's.");
                return;
            }
            logger.info("Processing event for %s notifications.", event.getNotificationIds().size());
            final List<AlertNotificationWrapper> notifications = notificationManager.findByIds(event.getNotificationIds());
            final List<DistributionEvent> distributionEvents = notificationProcessor.processNotifications(FrequencyType.REAL_TIME, notifications);
            eventManager.sendEvents(distributionEvents);
        } else {
            logger.warn("Received an event of type '{}', but this listener is for type '{}'.", event.getDestination(), NotificationEvent.NOTIFICATION_EVENT_TYPE);
        }
    }
}
