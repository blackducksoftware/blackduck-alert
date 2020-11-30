/**
 * alert-common
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
package com.synopsys.integration.alert.common.workflow.processor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.rest.RestConstants;

@Component
public class NotificationToDistributionEventConverter {
    private final Logger logger = LoggerFactory.getLogger(NotificationToDistributionEventConverter.class);
    private final ConfigurationAccessor configurationAccessor;
    private final DescriptorMap descriptorMap;

    @Autowired
    public NotificationToDistributionEventConverter(ConfigurationAccessor configurationAccessor, DescriptorMap descriptorMap) {
        this.configurationAccessor = configurationAccessor;
        this.descriptorMap = descriptorMap;
    }

    public List<DistributionEvent> convertToEvents(ConfigurationJobModel job, List<MessageContentGroup> messages) {
        Map<String, ConfigurationFieldModel> globalFields = Map.of();
        try {
            String descriptorName = job.getChannelName();
            DescriptorKey descriptorKey = descriptorMap.getDescriptorKey(descriptorName).orElseThrow(() -> new AlertException("Could not find a Descriptor with the name: " + descriptorName));
            globalFields = getGlobalFields(descriptorKey);
        } catch (AlertException e) {
            logger.error(e.getMessage());
        }
        job.getFieldUtility().addFields(globalFields);
        List<DistributionEvent> events = messages
                                             .stream()
                                             .map(message -> createChannelEvent(job, message))
                                             .collect(Collectors.toList());
        logger.debug("Created {} events for job: {}", events.size(), job.getName());
        return events;
    }

    private Map<String, ConfigurationFieldModel> getGlobalFields(DescriptorKey descriptorKey) {
        List<ConfigurationModel> globalConfiguration = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey, ConfigContextEnum.GLOBAL);
        return globalConfiguration.stream().findFirst().map(ConfigurationModel::getCopyOfKeyToFieldMap).orElse(Map.of());
    }

    private DistributionEvent createChannelEvent(ConfigurationJobModel job, MessageContentGroup contentGroup) {
        // TODO fix date usage
        return new DistributionEvent(job.getJobId().toString(), job.getChannelName(), RestConstants.formatDate(new Date()), job.getProviderConfigIdAsLong(), job.getProcessingType().name(), contentGroup, job.getFieldUtility());
    }

}
