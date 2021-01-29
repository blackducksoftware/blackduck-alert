/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
