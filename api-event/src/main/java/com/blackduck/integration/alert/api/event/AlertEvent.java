/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.event;

import java.util.UUID;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class AlertEvent extends AlertSerializableModel {
    private final String eventId;
    private final String destination;

    public AlertEvent(String destination) {
        this.eventId = UUID.randomUUID().toString();
        this.destination = destination;
    }

    public String getEventId() {
        return eventId;
    }

    public String getDestination() {
        return destination;
    }

}
