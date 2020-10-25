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
package com.synopsys.integration.alert.provider.blackduck.factories;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationClassMap;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.filter.BlackDuckDistributionFilter;
import com.synopsys.integration.alert.provider.blackduck.filter.BlackDuckProjectNameExtractor;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;

@Component
public class DistributionFilterFactory {
    private final Logger logger = LoggerFactory.getLogger(DistributionFilterFactory.class);
    private final Gson gson;

    @Autowired
    public DistributionFilterFactory(Gson gson) {
        this.gson = gson;
    }

    public ProviderDistributionFilter createFilter(BlackDuckProperties providerProperties, ProviderNotificationClassMap providerNotificationClassMap) {
        BlackDuckBucketService blackDuckBucketService = null;
        Optional<BlackDuckHttpClient> optionalBlackDuckHttpClient = providerProperties.createBlackDuckHttpClientAndLogErrors(logger);
        if (optionalBlackDuckHttpClient.isPresent()) {
            BlackDuckHttpClient blackDuckHttpClient = optionalBlackDuckHttpClient.get();
            BlackDuckServicesFactory blackDuckServicesFactory = providerProperties.createBlackDuckServicesFactory(blackDuckHttpClient, blackDuckHttpClient.getLogger());
            blackDuckBucketService = blackDuckServicesFactory.createBlackDuckBucketService();
        }

        BlackDuckProjectNameExtractor nameExtractor = new BlackDuckProjectNameExtractor(blackDuckBucketService, providerProperties.getBlackDuckTimeout());
        return new BlackDuckDistributionFilter(gson, providerNotificationClassMap, nameExtractor);
    }
}
