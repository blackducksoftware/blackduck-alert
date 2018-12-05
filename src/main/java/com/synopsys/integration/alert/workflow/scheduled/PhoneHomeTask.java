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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.descriptor.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.descriptor.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.phonehome.PhoneHomeCallable;
import com.synopsys.integration.phonehome.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.PhoneHomeService;

@Component
public class PhoneHomeTask extends ScheduledTask {
    public static final String TASK_NAME = "phonehome";
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
        final Optional<BlackduckRestConnection> optionalRestConnection = blackDuckProperties.createRestConnectionAndLogErrors(logger);
        if (optionalRestConnection.isPresent()) {
            final ExecutorService executorService = Executors.newSingleThreadExecutor();
            try (final BlackduckRestConnection restConnection = optionalRestConnection.get()) {
                final HubServicesFactory hubServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
                // TODO refactor to create a PhoneHomeCallable for alert.  May phone home per provider and channel.
                final PhoneHomeService phoneHomeService = hubServicesFactory.createPhoneHomeService(executorService);
                final Optional<PhoneHomeCallable> callable = createPhoneHomeCallable(hubServicesFactory);
                if (callable.isPresent()) {
                    phoneHomeService.phoneHome(callable.get());
                }

            } catch (final IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                executorService.shutdownNow();
            }
        }
    }

    public Optional<PhoneHomeCallable> createPhoneHomeCallable(final HubServicesFactory hubServicesFactory) {
        final String productVersion = aboutReader.getProductVersion();
        if (AboutReader.PRODUCT_VERSION_UNKNOWN.equals(productVersion)) {
            logger.debug("Unknown version for phone home");
            return Optional.empty();
        }
        final PhoneHomeRequestBody.Builder builder = new PhoneHomeRequestBody.Builder();
        addChannelMetaData(builder);
        try {
            final Optional<String> blackDuckUrl = blackDuckProperties.getBlackDuckUrl();
            if (blackDuckUrl.isPresent()) {
                return Optional.of(hubServicesFactory.createBlackDuckPhoneHomeCallable(new URL(blackDuckUrl.get()), "blackduck-alert", productVersion, builder));
            } else {
                return Optional.empty();
            }
        } catch (final MalformedURLException ex) {
            logger.error("Cannot create phone home callable", ex);
            return Optional.empty();
        }
    }

    public PhoneHomeRequestBody.Builder addChannelMetaData(final PhoneHomeRequestBody.Builder phoneHomeRequestBody) {
        final Map<String, Integer> createdSupportedChannels = getChannelMetaData();
        createdSupportedChannels.forEach((key, count) -> {
            final String supportedChannelkey = "channel." + key;
            phoneHomeRequestBody.addToMetaData(supportedChannelkey, count.toString());
        });

        return phoneHomeRequestBody;
    }

    private Map<String, Integer> getChannelMetaData() {
        final Set<String> descriptorNames = descriptorMap.getDescriptorMap().keySet();
        final Map<String, Integer> createdSupportedChannels = new HashMap<>();
        for (final String name : descriptorNames) {
            try {
                final List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationsByName(name);
                createdSupportedChannels.put(name, configurationModels.size());
            } catch (final AlertDatabaseConstraintException e) {
                logger.debug("Error reading from DB when phoning home.");
            }
        }

        return createdSupportedChannels;
    }

}
