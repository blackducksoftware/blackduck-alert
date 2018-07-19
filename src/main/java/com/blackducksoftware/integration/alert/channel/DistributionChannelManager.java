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
package com.blackducksoftware.integration.alert.channel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.exception.IntegrationException;

@Transactional
@Component
public class DistributionChannelManager {
    private final Logger logger = LoggerFactory.getLogger(DistributionChannelManager.class);
    private final ContentConverter contentConverter;

    @Autowired
    public DistributionChannelManager(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    public ContentConverter getContentConverter() {
        return contentConverter;
    }

    public String testGlobalConfig(final DatabaseEntity globalConfigEntity, final ChannelDescriptor descriptor) throws IntegrationException {
        descriptor.testGlobalConfig(globalConfigEntity);
        return "Successfully sent test message";
    }

    public String sendTestMessage(final CommonDistributionConfigRestModel restModel, final ChannelDescriptor descriptor) throws AlertException {
        final String destinationName = descriptor.getDestinationName();
        if (descriptor.hasGlobalConfiguration()) {
            if (descriptor.getGlobalRepositoryAccessor().readEntities().isEmpty()) {
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
        return new ChannelEvent(destination, contentConverter.getStringValue(content), commonDistributionConfigId);
    }
}
