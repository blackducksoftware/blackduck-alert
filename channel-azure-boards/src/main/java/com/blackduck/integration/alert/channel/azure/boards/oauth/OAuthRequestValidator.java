/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.oauth;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OAuthRequestValidator {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<UUID, OAuthRequestMapping> requestMap = new ConcurrentHashMap<>();

    public UUID generateRequestKey() {
        return UUID.randomUUID();
    }

    public void addAuthorizationRequest(UUID requestKey, UUID configurationId) {
        removeRequestsOlderThan5MinutesAgo();
        if (requestKey == null) {
            logger.error("OAuth authorization key is null, authorization request will not be added");
            return;
        }
        requestMap.entrySet().removeIf(entry -> entry.getValue().getConfigurationId().equals(configurationId));
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
        removeRequestsOlderThan5MinutesAgo();
    }

    public boolean hasRequestKey(UUID requestKey) {
        removeRequestsOlderThan5MinutesAgo();
        if (requestKey == null) {
            return false;
        }
        return requestMap.containsKey(requestKey);
    }

    public boolean hasRequests() {
        removeRequestsOlderThan5MinutesAgo();
        return !requestMap.isEmpty();
    }

    public UUID getConfigurationIdFromRequest(UUID requestKey) {
        removeRequestsOlderThan5MinutesAgo();
        return requestMap.get(requestKey).getConfigurationId();
    }

    public void removeRequestsOlderThanInstant(Instant instant) {
        requestMap.entrySet().stream()
            .filter(entry -> entry.getValue().getRequestTimestamp().isBefore(instant))
            .map(Map.Entry::getKey)
            .forEach(this::removeAuthorizationRequest);
    }

    public void removeRequestsOlderThan5MinutesAgo() {
        Instant fiveMinutesAgo = Instant.now().minusSeconds(300);
        removeRequestsOlderThanInstant(fiveMinutesAgo);
    }
}
