/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
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
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;

import com.synopsys.integration.alert.api.channel.DistributionEventReceiver;
import com.synopsys.integration.alert.api.event.AlertMessageListener;

@Configuration
public class EventListenerConfigurer implements JmsListenerConfigurer {
    private final Logger logger = LoggerFactory.getLogger(EventListenerConfigurer.class);

    private final List<AlertMessageListener<?>> allAlertMessageListeners;
    private final Set<String> distributionEventDestinationNames;
    private final CachingConnectionFactory cachingConnectionFactory;

    @Autowired
    public EventListenerConfigurer(
        List<AlertMessageListener<?>> allAlertMessageListeners,
        List<DistributionEventReceiver<?>> distributionEventReceivers,
        CachingConnectionFactory cachingConnectionFactory
    ) {
        this.allAlertMessageListeners = allAlertMessageListeners;
        this.distributionEventDestinationNames = distributionEventReceivers
            .stream()
            .map(AlertMessageListener::getDestinationName)
            .collect(Collectors.toSet());
        this.cachingConnectionFactory = cachingConnectionFactory;
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        MessageListenerContainer alertDefaultMessageListenerContainer = createMessageListenerContainer();

        logger.info("Registering JMS Listeners");
        for (AlertMessageListener<?> messageListener : allAlertMessageListeners) {
            if (distributionEventDestinationNames.contains(messageListener.getDestinationName())) {
                MessageListenerContainer distributionChannelMessageListenerContainer = createMessageListenerContainer();
                registerListenerEndpoint(registrar, messageListener, distributionChannelMessageListenerContainer);
            } else {
                registerListenerEndpoint(registrar, messageListener, alertDefaultMessageListenerContainer);
            }
        }
    }

    private MessageListenerContainer createMessageListenerContainer() {
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(cachingConnectionFactory);
        return messageListenerContainer;
    }

    private void registerListenerEndpoint(JmsListenerEndpointRegistrar registrar, AlertMessageListener<?> listener, MessageListenerContainer messageListenerContainer) {
        String destinationName = listener.getDestinationName();
        String listenerId = createListenerId(destinationName);
        logger.info("Registering JMS Listener: {}", listenerId);
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId(listenerId);
        endpoint.setDestination(destinationName);
        endpoint.setMessageListener(listener);
        endpoint.setupListenerContainer(messageListenerContainer);
        registrar.registerEndpoint(endpoint);
    }

    private String createListenerId(String name) {
        return String.format("%sListener", name);
    }

}
