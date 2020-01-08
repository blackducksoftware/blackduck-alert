/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.workflow;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public abstract class MessageReceiver<T> implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(MessageReceiver.class);
    private final Gson gson;
    private final Class<T> eventClass;

    public MessageReceiver(Gson gson, Class<T> eventClass) {
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
                logger.debug("{} event {}", receiverClassName, event);
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
