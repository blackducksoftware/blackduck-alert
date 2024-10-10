/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.oauth;

import java.time.Instant;
import java.util.UUID;

public class OAuthRequestMapping {
    private UUID configurationId;
    private Instant requestTimestamp;

    public OAuthRequestMapping(UUID configurationId, Instant requestTimestamp) {
        this.configurationId = configurationId;
        this.requestTimestamp = requestTimestamp;
    }

    public UUID getConfigurationId() {
        return configurationId;
    }

    public Instant getRequestTimestamp() {
        return requestTimestamp;
    }
}
