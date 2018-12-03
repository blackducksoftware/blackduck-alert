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
package com.synopsys.integration.alert.database.channel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.descriptor.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.descriptor.ConfigurationAccessor.ConfigurationModel;

@Component
public class JobConfigReader {
    private final ConfigurationAccessor configurationAccessor;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public JobConfigReader(final ConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    @Transactional
    public List<CommonDistributionConfiguration> getPopulatedConfigs() {
        final String descriptorType = DescriptorType.CHANNEL.name();
        try {
            final List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationsByType(descriptorType);
            return configurationModels.stream()
                       .map(configurationModel -> new CommonDistributionConfiguration(configurationModel))
                       .collect(Collectors.toList());
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("Was not able to retrieve configurations", e);
            return Collections.emptyList();
        }
    }

    @Transactional
    public Optional<CommonDistributionConfiguration> getPopulatedConfig(final Long configId) {
        if (null == configId) {
            return Optional.empty();
        }

        try {
            final ConfigurationModel configurationModel = configurationAccessor.getConfigurationById(configId);
            return Optional.of(new CommonDistributionConfiguration(configurationModel));
        } catch (final AlertDatabaseConstraintException e) {
            return Optional.empty();
        }
    }

}
