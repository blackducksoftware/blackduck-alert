/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.oauth;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OAuthRequestValidator {
    private static final Long TIME_TO_LIVE_IN_MILLIS = 600000L;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<UUID, OAuthRequestMapping> requestMap = Collections.synchronizedMap(new PassiveExpiringMap<>(TIME_TO_LIVE_IN_MILLIS));

    public UUID generateRequestKey() {
        return UUID.randomUUID();
    }

    public void addAuthorizationRequest(UUID requestKey, UUID configurationId) {
        if (requestKey == null) {
            logger.error("OAuth authorization key is null, authorization request will not be added");
            return;
        }
        logger.debug("Adding OAuth authorization key {}", requestKey);
        requestMap.put(requestKey, new OAuthRequestMapping(configurationId, Instant.now()));
    }

    public void removeAuthorizationRequest(UUID requestKey) {
        if (requestKey == null) {
            logger.error("OAuth authorization key is null, authorization request will not be removed");
            return;
        }
        requestMap.remove(requestKey);
        logger.debug("Removed OAuth authorization key {}", requestKey);
    }

    public boolean hasRequestKey(UUID requestKey) {
        if (requestKey == null) {
            return false;
        }
        return requestMap.containsKey(requestKey);
    }

    public boolean hasRequestFromConfigurationId(String configurationId) {
        return requestMap.values().stream()
            .map(OAuthRequestMapping::getConfigurationId)
            .map(UUID::toString)
            .anyMatch(configurationId::equals);
    }

    public boolean hasRequests() {
        return !requestMap.isEmpty();
    }

    public UUID getConfigurationIdFromRequest(UUID requestKey) {
        return requestMap.get(requestKey).getConfigurationId();
    }
}
