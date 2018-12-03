/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel;

import java.util.List;

import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;

@Configuration
public class ChannelListener implements JmsListenerConfigurer {
    private final Logger logger = LoggerFactory.getLogger(ChannelListener.class);

    private final List<ChannelDescriptor> channelDescriptorList;

    @Autowired
    public ChannelListener(final List<ChannelDescriptor> channelDescriptorList) {
        this.channelDescriptorList = channelDescriptorList;
    }

    @Override
    public void configureJmsListeners(final JmsListenerEndpointRegistrar registrar) {
        logger.info("Registering JMS Listeners");
        channelDescriptorList.forEach(descriptor -> {
            final MessageListener channelListener = descriptor.getChannelListener();
            if (channelListener != null) {
                final String listenerId = createListenerId(descriptor.getName());
                logger.info("Registering JMS Listener: {}", listenerId);
                final SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
                endpoint.setId(listenerId);
                endpoint.setDestination(descriptor.getDestinationName());
                endpoint.setMessageListener(channelListener);
                registrar.registerEndpoint(endpoint);
            }
        });
    }

    private String createListenerId(final String name) {
        final String listenerId = String.format("%sListener", name);
        return listenerId;
    }
}
