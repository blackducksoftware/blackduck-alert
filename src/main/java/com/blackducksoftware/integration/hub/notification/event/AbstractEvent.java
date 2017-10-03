package com.blackducksoftware.integration.hub.notification.event;

import java.util.UUID;

public abstract class AbstractEvent {

    private final String eventId;

    public AbstractEvent() {
        this.eventId = UUID.randomUUID().toString();
    }

    public String getEventId() {
        return eventId;
    }

    public abstract String getTopic();
}
