/*
 * api-event
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class EventManager {
    private final Gson gson;
    private final JmsTemplate jmsTemplate;

    @Autowired
    public EventManager(Gson gson, JmsTemplate jmsTemplate) {
        this.gson = gson;
        this.jmsTemplate = jmsTemplate;
    }

    public void sendEvents(List<? extends AlertEvent> eventList) {
        if (!eventList.isEmpty()) {
            eventList.forEach(this::sendEvent);
        }
    }

    public void sendEvent(AlertEvent event) {
        String destination = event.getDestination();
        String jsonMessage = toJsonOrNull(event);
        jmsTemplate.convertAndSend(destination, jsonMessage);
    }

    private String toJsonOrNull(Object content) {
        if (content != null) {
            return gson.toJson(content);
        }
        return null;
    }

}
