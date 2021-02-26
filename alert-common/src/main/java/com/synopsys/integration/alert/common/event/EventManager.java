/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;

@Component
public class EventManager {
    private final JmsTemplate jmsTemplate;
    private final ContentConverter contentConverter;

    @Autowired
    public EventManager(ContentConverter contentConverter, JmsTemplate jmsTemplate) {
        this.contentConverter = contentConverter;
        this.jmsTemplate = jmsTemplate;
    }

    public void sendEvents(List<? extends AlertEvent> eventList) {
        if (!eventList.isEmpty()) {
            eventList.forEach(this::sendEvent);
        }
    }

    public void sendEvent(AlertEvent event) {
        String destination = event.getDestination();
        String jsonMessage = contentConverter.getJsonString(event);
        jmsTemplate.convertAndSend(destination, jsonMessage);
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public ContentConverter getContentConverter() {
        return contentConverter;
    }

}
