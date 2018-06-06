package com.blackducksoftware.integration.hub.alert.event;

public enum InternalEventTypes {
    DB_STORE_EVENT("DB_STORE_EVENT"), REAL_TIME_EVENT("REAL_TIME_EVENT");

    private final String destination;

    InternalEventTypes(final String destination) {
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

}
