/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck;

import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.Slf4jIntLogger;

//TODO replace the usage of the class the cach client stored here isn't used at all.
/**
 * Caches a BlackDuckHttpClient for 2 minutes unless the cache is invalidated.
 */
@Component
@Deprecated
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
            .orElseThrow(() -> new AlertConfigurationException(String.format("No Black Duck configuration with id %s", blackDuckConfigId)));
        BlackDuckHttpClient blackDuckCacheClient = properties.createBlackDuckCacheClient(new Slf4jIntLogger(logger));
        httpClientCache.put(blackDuckConfigId, blackDuckCacheClient);
        return blackDuckCacheClient;
    }

}
