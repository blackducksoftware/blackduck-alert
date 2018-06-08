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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.blackducksoftware.integration.hub.alert.channel.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackChannel;

@Configuration
public class DescriptorConfig {
    private final ApplicationContext applicationContext;

    @Autowired
    public DescriptorConfig(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public List<ChannelDescriptor> channelDescriptorList() {
        final ChannelDescriptor emailChannelDescriptor = createDescriptor(EmailGroupChannel.COMPONENT_NAME).get();
        final ChannelDescriptor hipChatChannelDescriptor = createDescriptor(HipChatChannel.COMPONENT_NAME).get();
        final ChannelDescriptor slackChannelDescriptor = createDescriptor(SlackChannel.COMPONENT_NAME).get();

        return Arrays.asList(emailChannelDescriptor, hipChatChannelDescriptor, slackChannelDescriptor);
    }

    private Optional<ChannelDescriptor> createDescriptor(final String channelName) {
        final Object channelBean = applicationContext.getBean(channelName);
        final Class<DistributionChannel> distributionChannelClass = DistributionChannel.class;
        if (distributionChannelClass.isAssignableFrom(channelBean.getClass())) {
            final DistributionChannel distributionChannel = distributionChannelClass.cast(channelBean);
            return Optional.of(new ChannelDescriptor(channelName, channelName, distributionChannel));
        } else {
            return Optional.empty();
        }
    }
}
