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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

import com.blackducksoftware.integration.hub.alert.channel.ChannelDescriptor;

@Configuration
public class JMSListenerConfig implements JmsListenerConfigurer {

    private List<ChannelDescriptor> channelDescriptorList;

    @Autowired
    public JMSListenerConfig(List<ChannelDescriptor> channelDescriptorList) {
        this.channelDescriptorList = channelDescriptorList;
    }

    public void configureJmsListeners(final JmsListenerEndpointRegistrar registrar) {
        channelDescriptorList.forEach(descriptor -> {
            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
            final String listenerId = String.format("%sListener", descriptor.getName());
            endpoint.setId(listenerId);
            endpoint.setDestination(descriptor.getDestinationName());
            //endpoint.setMessageListener(descripto);
        });
    }
}
