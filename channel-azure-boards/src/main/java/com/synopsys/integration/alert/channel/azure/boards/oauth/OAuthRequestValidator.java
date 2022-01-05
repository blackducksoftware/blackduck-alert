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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OAuthRequestValidator {
    public static final String OAUTH_REQUEST_KEY_PREFIX = "alert-oauth-request-";
    public static final String UNKNOWN_OAUTH_ID = "<unknown value>";
    private final Logger logger = LoggerFactory.getLogger(OAuthRequestValidator.class);
    private final Map<String, Instant> requestMap = new ConcurrentHashMap<>();

    public String generateRequestKey() {
        UUID requestID = UUID.randomUUID();
        return String.format("%s%s", OAuthRequestValidator.OAUTH_REQUEST_KEY_PREFIX, requestID);
    }

    public void addAuthorizationRequest(String requestKey) {
        if (requestKey == null) {
            logger.error("OAuth authorization key is null, authorization request will not be added");
            return;
        }
        String oauthRequestId = parseRequestIdString(requestKey);
        logger.debug("Adding OAuth authorization key {}", oauthRequestId);
        requestMap.put(requestKey, Instant.now());
    }

    public void removeAuthorizationRequest(String requestKey) {
        if (requestKey == null) {
            logger.error("OAuth authorization key is null, authorization request will not be removed");
            return;
        }
        requestMap.remove(requestKey);
        String oauthRequestId = parseRequestIdString(requestKey);
        logger.debug("Removed OAuth authorization key {}", oauthRequestId);
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

    //TODO Currently being used by AzureBoards to clear all requests when the user deletes a config. Will need to update this when new OAuth user is introduced
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

    public void removeRequestsOlderThan5MinutesAgo() {
        Instant fiveMinutesAgo = Instant.now().minusSeconds(300);
        removeRequestsOlderThanInstant(fiveMinutesAgo);
    }

    public String parseRequestIdString(String userRequestKey) {
        return parseRequestKey(userRequestKey)
            .map(UUID::toString)
            .orElse(UNKNOWN_OAUTH_ID);
    }

    private Optional<UUID> parseRequestKey(String userRequestKey) {
        Optional<UUID> parsedKey = Optional.empty();
        if (StringUtils.isNotBlank(userRequestKey)) {
            String idString = StringUtils.remove(userRequestKey, OAUTH_REQUEST_KEY_PREFIX);
            try {
                parsedKey = Optional.of(UUID.fromString(idString));
            } catch (IllegalArgumentException ex) {
                logger.error("Error parsing OAuth UUID string", ex);
            }
        }
        return parsedKey;
    }
}
