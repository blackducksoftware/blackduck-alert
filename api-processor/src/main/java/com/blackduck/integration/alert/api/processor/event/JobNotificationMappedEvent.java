/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.event;

import java.util.UUID;

import com.blackduck.integration.alert.api.event.AlertEvent;

public class JobNotificationMappedEvent extends AlertEvent {
    private static final long serialVersionUID = 98931335736424452L;
    public static final String NOTIFICATION_MAPPED_EVENT_TYPE = "event_notification_mapped";

    private final UUID correlationId;

    public JobNotificationMappedEvent(UUID correlationId) {
        super(NOTIFICATION_MAPPED_EVENT_TYPE);
        this.correlationId = correlationId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }
}
