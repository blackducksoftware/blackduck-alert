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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.channel.CommonConfigurationModel;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

@Component
public class NotificationToChannelEventConverter {
    private final Logger logger = LoggerFactory.getLogger(NotificationToChannelEventConverter.class);
    private final DescriptorMap descriptorMap;

    @Autowired
    public NotificationToChannelEventConverter(final DescriptorMap descriptorMap) {
        this.descriptorMap = descriptorMap;
    }

    public List<DistributionEvent> convertToEvents(final Map<CommonConfigurationModel, List<AggregateMessageContent>> messageContentMap) {
        final List<DistributionEvent> distributionEvents = new ArrayList<>();
        final Set<? extends Map.Entry<CommonConfigurationModel, List<AggregateMessageContent>>> jobMessageContentEntries = messageContentMap.entrySet();
        for (final Map.Entry<CommonConfigurationModel, List<AggregateMessageContent>> entry : jobMessageContentEntries) {
            final CommonConfigurationModel jobConfig = entry.getKey();
            final List<AggregateMessageContent> contentList = entry.getValue();
            for (final AggregateMessageContent content : contentList) {
                distributionEvents.add(createChannelEvent(jobConfig, content));
            }
        }
        logger.debug("Created {} events.", distributionEvents.size());
        return distributionEvents;
    }

    private DistributionEvent createChannelEvent(final CommonConfigurationModel config, final AggregateMessageContent messageContent) {
        return descriptorMap.getChannelDescriptor(config.getChannelName()).getChannelEventProducer().createChannelEvent(config, messageContent);
    }
}
