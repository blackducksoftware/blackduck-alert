/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck;

import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

/**
 * Caches a BlackDuckHttpClient for 2 minutes unless the cache is invalidated.
 */
@Component
public class BlackDuckCacheHttpClientCache {
    private static final Long TIME_TO_LIVE_MINUTES = 2L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;
    private final PassiveExpiringMap<Long, BlackDuckHttpClient> httpClientCache;

    public BlackDuckCacheHttpClientCache(BlackDuckPropertiesFactory blackDuckPropertiesFactory) {
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
        this.httpClientCache = new PassiveExpiringMap<>(TIME_TO_LIVE_MINUTES, TimeUnit.MINUTES);
    }

    public void invalidate(Long blackDuckConfigId) {
        httpClientCache.remove(blackDuckConfigId);
    }

    public BlackDuckHttpClient retrieveOrCreateBlackDuckCacheHttpClient(Long blackDuckConfigId) throws IntegrationException {
        BlackDuckHttpClient cachedClient = httpClientCache.get(blackDuckConfigId);
        if (null != cachedClient) {
            return cachedClient;
        }

        BlackDuckProperties properties = blackDuckPropertiesFactory.createProperties(blackDuckConfigId)
                                             .orElseThrow(() -> new AlertConfigurationException(String.format("No BlackDuck configuration with id %s", blackDuckConfigId)));
        BlackDuckHttpClient blackDuckCacheClient = properties.createBlackDuckCacheClient(new Slf4jIntLogger(logger));
        httpClientCache.put(blackDuckConfigId, blackDuckCacheClient);
        return blackDuckCacheClient;
    }

}
