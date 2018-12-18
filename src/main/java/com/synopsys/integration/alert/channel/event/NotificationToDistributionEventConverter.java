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
package com.synopsys.integration.alert.channel.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.configuration.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;

@Component
public class NotificationToDistributionEventConverter {
    private final Logger logger = LoggerFactory.getLogger(NotificationToDistributionEventConverter.class);
    private final DescriptorMap descriptorMap;

    @Autowired
    public NotificationToDistributionEventConverter(final DescriptorMap descriptorMap) {
        this.descriptorMap = descriptorMap;
    }

    public List<DistributionEvent> convertToEvents(final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> messageContentMap) {
        final List<DistributionEvent> distributionEvents = new ArrayList<>();
        for (final Map.Entry<CommonDistributionConfiguration, List<AggregateMessageContent>> entry : messageContentMap.entrySet()) {
            for (final AggregateMessageContent content : entry.getValue()) {
                distributionEvents.add(createChannelEvent(entry.getKey(), content));
            }
        }
        logger.debug("Created {} events.", distributionEvents.size());
        return distributionEvents;
    }

    private DistributionEvent createChannelEvent(final CommonDistributionConfiguration config, final AggregateMessageContent messageContent) {
        final ChannelDescriptor channelDescriptor = descriptorMap.getChannelDescriptor(config.getChannelName());
        logger.info("Found descriptor {}", channelDescriptor.getName());
        final DescriptorActionApi actionApi = channelDescriptor.getRestApi(ConfigContextEnum.DISTRIBUTION);
        return actionApi.createChannelEvent(config, messageContent);
    }
}
