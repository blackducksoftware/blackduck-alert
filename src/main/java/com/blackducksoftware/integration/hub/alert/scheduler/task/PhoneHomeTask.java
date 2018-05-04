/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.scheduler.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.scheduler.AlertTaskScheduler;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.hub.service.model.PhoneHomeResponse;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;

@Component
public class PhoneHomeTask extends AlertTaskScheduler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GlobalProperties globalProperties;
    private final CommonDistributionRepositoryWrapper commonDistributionRepositoryWrapper;

    @Autowired
    public PhoneHomeTask(final TaskScheduler taskScheduler, final GlobalProperties globalProperties, final CommonDistributionRepositoryWrapper commonDistributionRepositoryWrapper) {
        super(taskScheduler);
        this.globalProperties = globalProperties;
        this.commonDistributionRepositoryWrapper = commonDistributionRepositoryWrapper;
    }

    @Override
    public void run() {
        final PhoneHomeResponse phoneHomeResponse = phoneHome();
        if (phoneHomeResponse != null) {
            phoneHomeResponse.endPhoneHome();
        }
    }

    private PhoneHomeResponse phoneHome() {
        final String productVersion = globalProperties.getProductVersion();
        if (!GlobalProperties.PRODUCT_VERSION_UNKNOWN.equals(productVersion)) {
            final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactoryAndLogErrors(logger);
            final PhoneHomeService phoneHomeService = hubServicesFactory.createPhoneHomeService();
            final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder = phoneHomeService.createInitialPhoneHomeRequestBodyBuilder("blackduck-alert", productVersion);
            final PhoneHomeRequestBody phoneHomeRequestBody = populateMetaData(phoneHomeRequestBodyBuilder).build();
            return phoneHomeService.startPhoneHome(phoneHomeRequestBody);
        } else {
            return null;
        }
    }

    private PhoneHomeRequestBody.Builder populateMetaData(final PhoneHomeRequestBody.Builder phoneHomeRequestBody) {
        final List<CommonDistributionConfigEntity> commonConfigList = commonDistributionRepositoryWrapper.findAll();
        final Map<String, Integer> createdSupportedChannels = new HashMap<>();
        for (final CommonDistributionConfigEntity commonConfigEntity : commonConfigList) {
            final String supportedChannel = commonConfigEntity.getDistributionType();
            if (createdSupportedChannels.containsKey(supportedChannel)) {
                final int count = createdSupportedChannels.get(supportedChannel);
                createdSupportedChannels.put(supportedChannel, count + 1);
            } else {
                createdSupportedChannels.put(supportedChannel, 0);
            }
        }

        for (final String supportedChannel : createdSupportedChannels.keySet()) {
            final Integer count = createdSupportedChannels.get(supportedChannel);
            final String supportedChannelkey = "channel." + supportedChannel;
            phoneHomeRequestBody.addToMetaData(supportedChannelkey, count.toString());
        }

        return phoneHomeRequestBody;
    }

}
