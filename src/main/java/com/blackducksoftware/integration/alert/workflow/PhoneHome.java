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
package com.blackducksoftware.integration.alert.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;

@Component
public class PhoneHome {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CommonDistributionRepository commonDistributionRepository;

    @Autowired
    public PhoneHome(final CommonDistributionRepository commonDistributionRepository) {
        this.commonDistributionRepository = commonDistributionRepository;
    }

    public PhoneHomeRequestBody.Builder createPhoneHomeBuilder(final PhoneHomeService phoneHomeService, final String productVersion) {
        if (BlackDuckProperties.PRODUCT_VERSION_UNKNOWN.equals(productVersion)) {
            logger.debug("Unknown version for phone home");
            return null;
        }

        return phoneHomeService.createInitialPhoneHomeRequestBodyBuilder("blackduck-alert", productVersion);
    }

    public PhoneHomeRequestBody.Builder addChannelMetaData(final PhoneHomeRequestBody.Builder phoneHomeRequestBody) {
        final Map<String, Integer> createdSupportedChannels = getChannelMetaData();
        createdSupportedChannels.entrySet().forEach(entry -> {
            final Integer count = entry.getValue();
            final String supportedChannelkey = "channel." + entry.getKey();
            phoneHomeRequestBody.addToMetaData(supportedChannelkey, count.toString());
        });

        return phoneHomeRequestBody;
    }

    private Map<String, Integer> getChannelMetaData() {
        final List<CommonDistributionConfigEntity> commonConfigList = commonDistributionRepository.findAll();
        final Map<String, Integer> createdSupportedChannels = new HashMap<>();
        for (final CommonDistributionConfigEntity commonConfigEntity : commonConfigList) {
            final String supportedChannel = commonConfigEntity.getDistributionType();
            if (createdSupportedChannels.containsKey(supportedChannel)) {
                final int count = createdSupportedChannels.get(supportedChannel);
                createdSupportedChannels.put(supportedChannel, count + 1);
            } else {
                createdSupportedChannels.put(supportedChannel, 1);
            }
        }

        return createdSupportedChannels;
    }

}
