/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.event;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

@Configuration
public class EventListenerConfigurer implements JmsListenerConfigurer {
    private final Logger logger = LoggerFactory.getLogger(EventListenerConfigurer.class);

    private final List<AlertEventListener> alertEventListeners;

    @Autowired
    public EventListenerConfigurer(final List<AlertEventListener> alertEventListeners) {
        this.alertEventListeners = alertEventListeners;
    }

    @Override
    public void configureJmsListeners(final JmsListenerEndpointRegistrar registrar) {
        logger.info("Registering JMS Listeners");
        for (final AlertEventListener listener : alertEventListeners) {
            if (listener != null) {
                final String destinationName = listener.getDestinationName();
                final String listenerId = createListenerId(destinationName);
                logger.info("Registering JMS Listener: {}", listenerId);
                final SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
                endpoint.setId(listenerId);
                endpoint.setDestination(destinationName);
                endpoint.setMessageListener(listener);
                registrar.registerEndpoint(endpoint);
            }
        }
    }

    private String createListenerId(final String name) {
        return String.format("%sListener", name);
    }
}
