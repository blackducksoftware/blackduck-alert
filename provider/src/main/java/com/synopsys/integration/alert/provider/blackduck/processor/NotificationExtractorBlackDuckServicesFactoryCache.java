/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.processor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.processor.api.NotificationProcessingLifecycleCache;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.Slf4jIntLogger;

/**
 * Caches {@link BlackDuckServicesFactory} by BlackDuck configuration ID.
 * Synchronized and backed by a {@link PassiveExpiringMap} the documentation for which
 * can be found <a href="https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/map/PassiveExpiringMap.html">here</a>.
 */
@Component
public class NotificationExtractorBlackDuckServicesFactoryCache implements NotificationProcessingLifecycleCache {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;
    private final Map<Long, BlackDuckServicesFactory> servicesFactoryCache;

    public NotificationExtractorBlackDuckServicesFactoryCache(BlackDuckPropertiesFactory blackDuckPropertiesFactory) {
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
        this.servicesFactoryCache = new ConcurrentHashMap<>();
    }

    public BlackDuckServicesFactory retrieveBlackDuckServicesFactory(Long blackDuckConfigId) throws AlertConfigurationException {
        BlackDuckServicesFactory blackDuckServicesFactory = servicesFactoryCache.get(blackDuckConfigId);
        if (null == blackDuckServicesFactory) {
            blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckConfigId);
        }

        servicesFactoryCache.put(blackDuckConfigId, blackDuckServicesFactory);
        return blackDuckServicesFactory;
    }

    private BlackDuckServicesFactory createBlackDuckServicesFactory(Long blackDuckConfigId) throws AlertConfigurationException {
        Optional<BlackDuckProperties> optionalProperties = blackDuckPropertiesFactory.createPropertiesIfConfigExists(blackDuckConfigId);
        if (optionalProperties.isPresent()) {
            BlackDuckProperties blackDuckProperties = optionalProperties.get();
            Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
            try {
                BlackDuckHttpClient blackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClient(intLogger);
                return blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, intLogger);
            } catch (AlertException e) {
                throw new AlertConfigurationException("The BlackDuck configuration is invalid", e);
            }
        }
        throw new AlertConfigurationException(String.format("No BlackDuck configuration with id '%s' existed", blackDuckConfigId));
    }

    @Override
    public void clear() {
        servicesFactoryCache.clear();
    }

}
