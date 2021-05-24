/*
 * api-event
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.event;

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
        String destinationName = listener.getDestinationName();
        String listenerId = createListenerId(destinationName);
        logger.info("Registering JMS Listener: {}", listenerId);
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId(listenerId);
        endpoint.setDestination(destinationName);
        endpoint.setMessageListener(listener);
        registrar.registerEndpoint(endpoint, jmsListenerContainerFactory);
    }

    private String createListenerId(String name) {
        return String.format("%sListener", name);
    }

}
