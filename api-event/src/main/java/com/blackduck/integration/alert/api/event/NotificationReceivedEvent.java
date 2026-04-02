/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.event;

import java.util.UUID;

public class NotificationReceivedEvent extends AlertEvent {

    public static final String NOTIFICATION_RECEIVED_EVENT_TYPE = "notification_received_event";
    private static final long serialVersionUID = -6352416816995294053L;

    private final UUID correlationId;
    private final Long providerConfigId;
    private final UUID batchId;

    public NotificationReceivedEvent(Long providerConfigId, UUID batchId) {
        this(UUID.randomUUID(), providerConfigId, batchId);
    }

    public NotificationReceivedEvent(UUID correlationId, Long providerConfigId, UUID batchId) {
        super(NOTIFICATION_RECEIVED_EVENT_TYPE);
        this.correlationId = correlationId;
        this.providerConfigId = providerConfigId;
        this.batchId = batchId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public UUID getBatchId() {
        return batchId;
    }
}
