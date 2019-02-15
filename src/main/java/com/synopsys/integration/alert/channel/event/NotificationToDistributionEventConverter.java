/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;

@Component
public class NotificationToDistributionEventConverter {
    private final Logger logger = LoggerFactory.getLogger(NotificationToDistributionEventConverter.class);
    private final DescriptorMap descriptorMap;
    private final BaseConfigurationAccessor configurationAccessor;

    @Autowired
    public NotificationToDistributionEventConverter(final DescriptorMap descriptorMap, final BaseConfigurationAccessor configurationAccessor) {
        this.descriptorMap = descriptorMap;
        this.configurationAccessor = configurationAccessor;
    }

    public List<DistributionEvent> convertToEvents(final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> messageContentMap) {
        final List<DistributionEvent> distributionEvents = new ArrayList<>();
        for (final Map.Entry<CommonDistributionConfiguration, List<AggregateMessageContent>> entry : messageContentMap.entrySet()) {
            for (final AggregateMessageContent content : entry.getValue()) {
                final CommonDistributionConfiguration config = entry.getKey();
                final String descriptorName = config.getChannelName();
                final Map<String, ConfigurationFieldModel> globalFields = getGlobalFields(descriptorName);
                config.addFields(globalFields);
                descriptorMap.getChannelDescriptor(descriptorName)
                    .flatMap(channelDescriptor -> channelDescriptor.getActionApi(ConfigContextEnum.DISTRIBUTION))
                    .map(descriptorActionApi -> descriptorActionApi.createChannelEvent(config, content))
                    .ifPresent(distributionEvents::add);
            }
        }
        logger.debug("Created {} events.", distributionEvents.size());
        return distributionEvents;
    }

    private Map<String, ConfigurationFieldModel> getGlobalFields(final String descriptorName) {
        try {
            final List<ConfigurationModel> globalConfiguration = configurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, ConfigContextEnum.GLOBAL);
            return globalConfiguration.stream().findFirst().map(ConfigurationModel::getCopyOfKeyToFieldMap).orElse(Map.of());
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("There was an error retrieving global config : {}", e.getMessage());
            return Map.of();
        }
    }

}
