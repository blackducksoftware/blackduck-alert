/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.oauth;

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

    public boolean hasRequests() {
        return !requestMap.isEmpty();
    }

    //TODO Currently being used by AzureBoards to clear all requests when the user deletes a config. Will need to update this when new OAuth user is introduced
    public void removeAllRequests() {
        // NOTE: If there are multiple OAuth clients make sure removeAllRequests is used correctly.
        // Do not want to remove requests for other OAuth clients inadvertently.
        requestMap.clear();
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

    public UUID getConfigurationIdFromRequest(UUID requestKey) {
        return requestMap.get(requestKey).getConfigurationId();
    }
}
