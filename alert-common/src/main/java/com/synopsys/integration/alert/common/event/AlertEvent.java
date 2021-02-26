/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.event;

import java.util.UUID;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

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
