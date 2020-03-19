/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.rest.RestConstants;

@Component
public class NotificationToDistributionEventConverter {
    private final Logger logger = LoggerFactory.getLogger(NotificationToDistributionEventConverter.class);
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public NotificationToDistributionEventConverter(final ConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    public List<DistributionEvent> convertToEvents(final Map<ConfigurationJobModel, List<MessageContentGroup>> messageContentMap) {
        final List<DistributionEvent> distributionEvents = new ArrayList<>();
        for (final Map.Entry<ConfigurationJobModel, List<MessageContentGroup>> entry : messageContentMap.entrySet()) {
            for (final MessageContentGroup contentGroup : entry.getValue()) {
                final ConfigurationJobModel config = entry.getKey();
                final String descriptorName = config.getChannelName();
                final Map<String, ConfigurationFieldModel> globalFields = getGlobalFields(descriptorName);
                config.getFieldAccessor().addFields(globalFields);
                distributionEvents.add(createChannelEvent(config, contentGroup));
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

    private DistributionEvent createChannelEvent(final ConfigurationJobModel commonDistributionConfig, final MessageContentGroup contentGroup) {
        return new DistributionEvent(commonDistributionConfig.getJobId().toString(), commonDistributionConfig.getChannelName(), RestConstants.formatDate(new Date()), commonDistributionConfig.getProviderName(),
            commonDistributionConfig.getFormatType().name(), contentGroup, commonDistributionConfig.getFieldAccessor());
    }

}
