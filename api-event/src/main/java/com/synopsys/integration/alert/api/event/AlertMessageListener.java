/*
 * api-event
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.event;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public abstract class AlertMessageListener<T extends AlertEvent> implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final String destinationName;
    private final Class<T> eventClass;
    private final AlertEventHandler<T> eventHandler;

    protected AlertMessageListener(Gson gson, String destinationName, Class<T> eventClass, AlertEventHandler<T> eventHandler) {
        this.gson = gson;
        this.destinationName = destinationName;
        this.eventClass = eventClass;
        this.eventHandler = eventHandler;
    }

    public final String getDestinationName() {
        return destinationName;
    }

    @Override
    public final void onMessage(Message message) {
        try {
            if (TextMessage.class.isAssignableFrom(message.getClass())) {
                String receiverClassName = getClass().getName();
                logger.info("Receiver {}, sending message.", receiverClassName);
                logger.debug("Event message: {}", message);
                TextMessage textMessage = (TextMessage) message;
                T event = gson.fromJson(textMessage.getText(), eventClass);
                logger.trace("{} event {}", receiverClassName, event);
                logger.debug("Received Event ID: {}", event.getEventId());
                eventHandler.handle(event);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
