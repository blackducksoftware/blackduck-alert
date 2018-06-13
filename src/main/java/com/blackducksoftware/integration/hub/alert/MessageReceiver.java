/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.alert;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.alert.event.AlertEvent;
import com.google.gson.Gson;

@Transactional
public abstract class MessageReceiver<E extends AlertEvent> implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(MessageReceiver.class);
    private final Class<E> clazz;
    private final Gson gson;

    public MessageReceiver(final Gson gson, final Class<E> clazz) {
        this.clazz = clazz;
        this.gson = gson;
    }

    public abstract void handleEvent(E event);

    @Override
    public void onMessage(final Message message) {
        try {
            if (TextMessage.class.isAssignableFrom(message.getClass())) {
                logger.info(String.format("Received %s event message: %s", getClass().getName(), message));
                final TextMessage textMessage = (TextMessage) message;
                final E event = gson.fromJson(textMessage.getText(), clazz);
                logger.info(String.format("%s event %s", getClass().getName(), event));
                handleEvent(event);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
