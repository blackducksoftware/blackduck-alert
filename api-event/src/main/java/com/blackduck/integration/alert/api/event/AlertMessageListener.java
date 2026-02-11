/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;

public abstract class AlertMessageListener<T extends AlertEvent> implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final TaskExecutor taskExecutor;
    private final String destinationName;
    private final Class<T> eventClass;
    private final AlertEventHandler<T> eventHandler;
    private final AtomicLong totalBytesCounter;
    private final AtomicLong messageCounter;

    protected AlertMessageListener(Gson gson, TaskExecutor taskExecutor, String destinationName, Class<T> eventClass, AlertEventHandler<T> eventHandler) {
        this.gson = gson;
        this.destinationName = destinationName;
        this.eventClass = eventClass;
        this.eventHandler = eventHandler;
        this.taskExecutor = taskExecutor;
        this.totalBytesCounter = new AtomicLong();
        this.messageCounter= new AtomicLong();
    }

    public final String getDestinationName() {
        return destinationName;
    }

    @Override
    public final void onMessage(Message message) {
        try {
            byte[] body = message.getBody();
            totalBytesCounter.addAndGet(body.length);
            messageCounter.incrementAndGet();
            String messageContent = new String(body);
            String receiverClassName = getClass().getName();
            logger.info("Receiver {}, sending message.", receiverClassName);
            logger.debug("Event message: {}", message);
            T event = gson.fromJson(messageContent, eventClass);
            logger.trace("{} event {}", receiverClassName, event);
            logger.debug("Received Event ID: {}", event.getEventId());
            taskExecutor.execute(() ->
                handleEvent(event)
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void handleEvent(T event) {
        try {
            eventHandler.handle(event);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public final Double calculateAverageMessageSize() {
        long totalBytes = totalBytesCounter.get();
        long messageCount = messageCounter.get();
        double averageMessageSize = 0.0;

        if (messageCount > 0 ) {
            double averageBytes = (double) totalBytes / messageCount;
            averageMessageSize = BigDecimal.valueOf(averageBytes).setScale(4, RoundingMode.HALF_UP).doubleValue();
        }

        return averageMessageSize;
    }

}
