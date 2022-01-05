/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.processor.api.NotificationProcessingLifecycleCache;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.Slf4jIntLogger;

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
