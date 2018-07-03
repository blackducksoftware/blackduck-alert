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
package com.blackducksoftware.integration.hub.alert.channel.manager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Transactional
@Component
public class DistributionChannelManager {
    private final Logger logger = LoggerFactory.getLogger(DistributionChannelManager.class);
    private final ObjectTransformer objectTransformer;
    private final AlertEventContentConverter contentConverter;
    private final Map<String, ChannelDescriptor> channelDescriptorMap;

    @Autowired
    public DistributionChannelManager(final ObjectTransformer objectTransformer, final AlertEventContentConverter contentConverter, final List<ChannelDescriptor> channelDescriptorList) {
        this.objectTransformer = objectTransformer;
        this.contentConverter = contentConverter;
        channelDescriptorMap = new HashMap<>(channelDescriptorList.size());

        channelDescriptorList.forEach(descriptor -> {
            channelDescriptorMap.put(descriptor.getDestinationName(), descriptor);
        });
    }

    public ObjectTransformer getObjectTransformer() {
        return objectTransformer;
    }

    public String testGlobalConfig(final ConfigRestModel restModel, final ChannelDescriptor descriptor) throws IntegrationException {
        final DatabaseEntity globalConfigEntity = descriptor.convertFromGlobalRestModelToGlobalConfigEntity(restModel);
        descriptor.testGlobalConfig(globalConfigEntity);
        return "Successfully sent test message";
    }

    public String sendTestMessage(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) throws AlertException {
        final String destinationName = descriptor.getDestinationName();
        if (descriptor.hasGlobalConfiguration()) {
            if (descriptor.readGlobalEntities().isEmpty()) {
                logger.error("Sending test message for destination {} failed. Missing global configuration for channel", destinationName);
                return "ERROR: Missing global configuration!";
            }
        }
        final ChannelEvent event = createChannelEvent(destinationName, getTestMessageModel(), null);
        try {
            descriptor.testDistributionConfig(restModel, event);
        } catch (final IntegrationException ex) {
            logger.error("Error sending test message for destination {} ", destinationName, ex);
            return ex.getMessage();
        }
        logger.info("Successfully sent test message for destination {} ", destinationName);
        return "Successfully sent test message";
    }

    public DigestModel getTestMessageModel() {
        final Collection<ProjectData> projectDataCollection = Arrays.asList(new ProjectData(DigestTypeEnum.REAL_TIME, "Hub Alert", "Test Message", Collections.emptyList(), Collections.emptyMap()));
        final DigestModel digestModel = new DigestModel(projectDataCollection);
        return digestModel;
    }

    public ChannelEvent createChannelEvent(final String destination, final DigestModel content, final Long commonDistributionConfigId) {
        return new ChannelEvent(destination, contentConverter.convertToString(content), commonDistributionConfigId);
    }
}
