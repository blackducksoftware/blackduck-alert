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
package com.synopsys.integration.alert.workflow.scheduled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.phonehome.BlackDuckPhoneHomeHelper;
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.phonehome.PhoneHomeResponse;

@Component
public class PhoneHomeTask extends ScheduledTask {
    public static final String TASK_NAME = "phonehome";
    public static final String ARTIFACT_ID = "blackduck-alert";
    public static final Long DEFAULT_TIMEOUT = 10L;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckProperties blackDuckProperties;
    private final AboutReader aboutReader;
    private final ConfigurationAccessor configurationAccessor;
    private final DescriptorMap descriptorMap;

    @Autowired
    public PhoneHomeTask(final TaskScheduler taskScheduler, final BlackDuckProperties blackDuckProperties, final AboutReader aboutReader, final ConfigurationAccessor configurationAccessor, final DescriptorMap descriptorMap) {
        super(taskScheduler, TASK_NAME);
        this.blackDuckProperties = blackDuckProperties;
        this.aboutReader = aboutReader;
        this.configurationAccessor = configurationAccessor;
        this.descriptorMap = descriptorMap;
    }

    @Override
    public void run() {
        final Optional<BlackDuckRestConnection> optionalRestConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
        if (optionalRestConnection.isPresent()) {
            final String productVersion = aboutReader.getProductVersion();
            if (AboutReader.PRODUCT_VERSION_UNKNOWN.equals(productVersion)) {
                return;
            }
            final ExecutorService phoneHomeExecutor = Executors.newSingleThreadExecutor();
            try {
                final BlackDuckRestConnection restConnection = optionalRestConnection.get();
                final BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
                final BlackDuckPhoneHomeHelper blackDuckPhoneHomeHelper = BlackDuckPhoneHomeHelper.createAsynchronousPhoneHomeHelper(blackDuckServicesFactory, phoneHomeExecutor);

                final Map<String, String> metaData = getChannelMetaData();
                final PhoneHomeResponse phoneHomeResponse = blackDuckPhoneHomeHelper.handlePhoneHome(ARTIFACT_ID, productVersion, metaData);
                phoneHomeResponse.awaitResult(DEFAULT_TIMEOUT);
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                phoneHomeExecutor.shutdownNow();
            }
        }
    }

    private Map<String, String> getChannelMetaData() {
        final Set<String> descriptorNames = descriptorMap.getDescriptorMap().keySet();
        final Map<String, Integer> createdSupportedChannels = new HashMap<>();
        for (final String name : descriptorNames) {
            try {
                final List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationsByDescriptorName(name);
                createdSupportedChannels.put(name, configurationModels.size());
            } catch (final AlertDatabaseConstraintException e) {
                logger.debug("Error reading from DB when phoning home.");
            }
        }
        return createdSupportedChannels
                       .entrySet()
                       .stream()
                       .collect(Collectors.toMap(Map.Entry::getKey, intValue -> intValue.toString()));
    }

}
