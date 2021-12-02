/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.event;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;

@Component
public class EventManager {
    private final Logger logger = LoggerFactory.getLogger(EventManager.class);
    private final JmsTemplate jmsTemplate;
    private final ContentConverter contentConverter;
    private final ExecutorService eventDispatcher;

    @Autowired
    public EventManager(ContentConverter contentConverter, JmsTemplate jmsTemplate) {
        this.contentConverter = contentConverter;
        this.jmsTemplate = jmsTemplate;
        this.eventDispatcher = Executors.newSingleThreadExecutor();
    }

    public void sendEvents(List<? extends AlertEvent> eventList) {
        if (!eventList.isEmpty()) {
            eventList.forEach(this::sendEvent);
        }
    }

    public void sendEvent(AlertEvent event) {
        Future<Void> eventDispatched = eventDispatcher.submit(() -> {
            String destination = event.getDestination();
            String jsonMessage = contentConverter.getJsonString(event);
            jmsTemplate.convertAndSend(destination, jsonMessage);
            return null;
        });

        try {
            // wait for event to be dispatched
            eventDispatched.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            logger.error("Error dispatching event: {}", event);
            logger.error("Cause", ex);
        }
    }

}
