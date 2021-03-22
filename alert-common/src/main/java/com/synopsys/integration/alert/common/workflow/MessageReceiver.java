/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.workflow;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.event.AlertEvent;

public abstract class MessageReceiver<T extends AlertEvent> implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final Class<T> eventClass;

    protected MessageReceiver(Gson gson, Class<T> eventClass) {
        this.gson = gson;
        this.eventClass = eventClass;
    }

    public abstract void handleEvent(T event);

    @Override
    public void onMessage(Message message) {
        try {
            if (TextMessage.class.isAssignableFrom(message.getClass())) {
                String receiverClassName = getClass().getName();
                logger.info("Receiver {}, sending message.", receiverClassName);
                logger.debug("Event message: {}", message);
                TextMessage textMessage = (TextMessage) message;
                T event = gson.fromJson(textMessage.getText(), eventClass);
                logger.trace("{} event {}", receiverClassName, event);
                logger.debug("Received Event ID: {}", event.getEventId());
                handleEvent(event);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public Gson getGson() {
        return gson;
    }

}
