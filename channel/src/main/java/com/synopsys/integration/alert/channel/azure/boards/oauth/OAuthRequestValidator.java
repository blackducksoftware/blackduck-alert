/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.oauth;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OAuthRequestValidator {
    private final Logger logger = LoggerFactory.getLogger(OAuthRequestValidator.class);
    private final Map<String, Instant> requestMap = new ConcurrentHashMap<>();

    public void addAuthorizationRequest(String requestKey) {
        if (requestKey == null) {
            logger.error("OAuth authorization key is null, authorization request will not be added");
            return;
        }
        logger.debug("Adding OAuth authorization key {}", requestKey);
        requestMap.put(requestKey, Instant.now());
    }

    public void removeAuthorizationRequest(String requestKey) {
        if (requestKey == null) {
            logger.error("OAuth authorization key is null, authorization request will not be removed");
            return;
        }
        requestMap.remove(requestKey);
        logger.debug("Removed OAuth authorization key {}", requestKey);
    }

    public boolean hasRequestKey(String requestKey) {
        if (requestKey == null) {
            return false;
        }
        return requestMap.containsKey(requestKey);
    }

    public boolean hasRequests() {
        return !requestMap.isEmpty();
    }

    public void removeAllRequests() {
        // NOTE: If there are multiple OAuth clients make sure removeAllRequests is used correctly.
        // Do not want to remove requests for other OAuth clients inadvertently.
        requestMap.clear();
    }

    public void removeRequestsOlderThanInstant(Instant instant) {
        requestMap.entrySet().stream()
            .filter(entry -> entry.getValue().isBefore(instant))
            .map(Map.Entry::getKey)
            .forEach(this::removeAuthorizationRequest);

    }
}
