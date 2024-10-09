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
