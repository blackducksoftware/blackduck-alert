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
package com.blackducksoftware.integration.hub.alert.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

import com.blackducksoftware.integration.hub.alert.accumulator.RealTimeListener;
import com.blackducksoftware.integration.hub.alert.channel.ChannelDescriptor;

@Configuration
public class JMSListenerConfig implements JmsListenerConfigurer {
    private final Logger logger = LoggerFactory.getLogger(JMSListenerConfig.class);

    private List<ChannelDescriptor> channelDescriptorList;
    private RealTimeListener realTimeListener;

    @Autowired
    public JMSListenerConfig(final List<ChannelDescriptor> channelDescriptorList, final RealTimeListener realTimeListener) {
        this.channelDescriptorList = channelDescriptorList;
        this.realTimeListener = realTimeListener;
    }

    public void configureJmsListeners(final JmsListenerEndpointRegistrar registrar) {
        logger.info("Registering JMS Listeners");
        SimpleJmsListenerEndpoint realTimeEndpoint = new SimpleJmsListenerEndpoint();
        realTimeEndpoint.setId(createListenerId("RealTime"));
        realTimeEndpoint.setDestination("REAL_TIME_EVENT");
        realTimeEndpoint.setMessageListener(realTimeListener);
        registrar.registerEndpoint(realTimeEndpoint);

        channelDescriptorList.forEach(descriptor -> {
            final String listenerId = createListenerId(descriptor.getName());
            logger.info("Registering JMS Listener: {}", listenerId);
            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
            endpoint.setId(listenerId);
            endpoint.setDestination(descriptor.getDestinationName());
            endpoint.setMessageListener(descriptor.getChannelComponent());
            registrar.registerEndpoint(endpoint);
        });
    }

    private String createListenerId(String name) {
        final String listenerId = String.format("%sListener", name);
        return listenerId;
    }
}
