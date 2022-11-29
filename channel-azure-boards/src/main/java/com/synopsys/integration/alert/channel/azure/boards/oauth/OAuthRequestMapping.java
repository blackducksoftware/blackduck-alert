package com.synopsys.integration.alert.channel.azure.boards.oauth;

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
