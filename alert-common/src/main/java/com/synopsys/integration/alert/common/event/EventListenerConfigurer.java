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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

@Configuration
public class EventListenerConfigurer implements JmsListenerConfigurer {
    private final Logger logger = LoggerFactory.getLogger(EventListenerConfigurer.class);

    private final List<AlertChannelEventListener> alertChannelEventListeners;
    private final List<AlertDefaultEventListener> alertDefaultEventListeners;
    private final DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory;
    private final DefaultJmsListenerContainerFactory distributionChannelJmsListenerContainerFactory;

    @Autowired
    public EventListenerConfigurer(List<AlertChannelEventListener> alertChannelEventListeners,
        List<AlertDefaultEventListener> alertDefaultEventListeners,
        DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory,
        DefaultJmsListenerContainerFactory distributionChannelJmsListenerContainerFactory) {
        this.alertChannelEventListeners = alertChannelEventListeners;
        this.alertDefaultEventListeners = alertDefaultEventListeners;
        this.defaultJmsListenerContainerFactory = defaultJmsListenerContainerFactory;
        this.distributionChannelJmsListenerContainerFactory = distributionChannelJmsListenerContainerFactory;
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        logger.info("Registering JMS Listeners");
        alertDefaultEventListeners.forEach(listener -> registerListenerEndpoint(registrar, listener, defaultJmsListenerContainerFactory));
        alertChannelEventListeners.forEach(listener -> registerListenerEndpoint(registrar, listener, distributionChannelJmsListenerContainerFactory));
    }

    private void registerListenerEndpoint(JmsListenerEndpointRegistrar registrar, AlertEventListener listener, DefaultJmsListenerContainerFactory jmsListenerContainerFactory) {
        if (listener != null) {
            String destinationName = listener.getDestinationName();
            String listenerId = createListenerId(destinationName);
            logger.info("Registering JMS Listener: {}", listenerId);
            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
            endpoint.setId(listenerId);
            endpoint.setDestination(destinationName);
            endpoint.setMessageListener(listener);
            registrar.registerEndpoint(endpoint, jmsListenerContainerFactory);
        }
    }

    private String createListenerId(String name) {
        return String.format("%sListener", name);
    }
}
