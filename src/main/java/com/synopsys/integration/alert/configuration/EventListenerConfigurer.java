/*
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.configuration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

import com.synopsys.integration.alert.api.event.AlertMessageListener;
import com.synopsys.integration.alert.channel.api.DistributionEventReceiver;

@Configuration
public class EventListenerConfigurer implements JmsListenerConfigurer {
    private final Logger logger = LoggerFactory.getLogger(EventListenerConfigurer.class);

    private final List<AlertMessageListener<?>> allAlertMessageListeners;
    private final Set<String> distributionEventDestinationNames;
    private final DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory;
    private final DefaultJmsListenerContainerFactory distributionChannelJmsListenerContainerFactory;

    @Autowired
    public EventListenerConfigurer(
        List<AlertMessageListener<?>> allAlertMessageListeners,
        List<DistributionEventReceiver<?>> distributionEventReceivers,
        DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory,
        DefaultJmsListenerContainerFactory distributionChannelJmsListenerContainerFactory
    ) {
        this.allAlertMessageListeners = allAlertMessageListeners;
        this.defaultJmsListenerContainerFactory = defaultJmsListenerContainerFactory;
        this.distributionChannelJmsListenerContainerFactory = distributionChannelJmsListenerContainerFactory;
        this.distributionEventDestinationNames = distributionEventReceivers
                                                     .stream()
                                                     .map(AlertMessageListener::getDestinationName)
                                                     .collect(Collectors.toSet());
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        logger.info("Registering JMS Listeners");
        for (AlertMessageListener<?> messageListener : allAlertMessageListeners) {
            if (distributionEventDestinationNames.contains(messageListener.getDestinationName())) {
                registerListenerEndpoint(registrar, messageListener, distributionChannelJmsListenerContainerFactory);
            } else {
                registerListenerEndpoint(registrar, messageListener, defaultJmsListenerContainerFactory);
            }
        }
    }

    private void registerListenerEndpoint(JmsListenerEndpointRegistrar registrar, AlertMessageListener<?> listener, DefaultJmsListenerContainerFactory jmsListenerContainerFactory) {
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
