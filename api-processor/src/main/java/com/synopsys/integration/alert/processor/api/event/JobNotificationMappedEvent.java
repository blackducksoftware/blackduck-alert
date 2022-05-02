/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.event;

import java.util.UUID;

import com.synopsys.integration.alert.api.event.AlertEvent;

public class JobNotificationMappedEvent extends AlertEvent {
    public static final String NOTIFICATION_MAPPED_EVENT_TYPE = "notification_mapped_event";
    private final UUID correlationId;

    public JobNotificationMappedEvent(UUID correlationId) {
        super(NOTIFICATION_MAPPED_EVENT_TYPE);
        this.correlationId = correlationId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }
}
