/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.NotificationProcessingLifecycleCache;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.log.Slf4jIntLogger;

/**
 * Caches {@link BlackDuckServicesFactory} by BlackDuck configuration ID.
 * Synchronized and backed by a {@link ConcurrentHashMap}.
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
        Optional<BlackDuckProperties> optionalProperties = blackDuckPropertiesFactory.createProperties(blackDuckConfigId);
        if (optionalProperties.isPresent()) {
            BlackDuckProperties blackDuckProperties = optionalProperties.get();
            Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
            try {
                BlackDuckHttpClient blackDuckHttpClient = blackDuckProperties.createBlackDuckCacheClient(intLogger);
                return blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, intLogger);
            } catch (AlertException e) {
                throw new AlertConfigurationException("The Black Duck configuration is invalid", e);
            }
        }
        throw new AlertConfigurationException(String.format("No Black Duck configuration with id '%s' existed", blackDuckConfigId));
    }

    @Override
    public void clear() {
        servicesFactoryCache.clear();
    }

}
