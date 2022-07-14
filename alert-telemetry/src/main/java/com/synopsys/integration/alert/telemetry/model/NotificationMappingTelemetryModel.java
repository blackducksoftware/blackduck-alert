/*
 * alert-telemetry
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.telemetry.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class NotificationMappingTelemetryModel extends AlertSerializableModel implements Obfuscated<NotificationMappingTelemetryModel> {
    private static final long serialVersionUID = 1131364695726016688L;
    private UUID correlationId;
    private OffsetDateTime startTaskTime;
    private OffsetDateTime completeTaskTime;

    public NotificationMappingTelemetryModel() {
        // For serialization
    }

    public NotificationMappingTelemetryModel(UUID correlationId, OffsetDateTime startTaskTime, @Nullable OffsetDateTime completeTaskTime) {
        this.correlationId = correlationId;
        this.startTaskTime = startTaskTime;
        this.completeTaskTime = completeTaskTime;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public OffsetDateTime getStartTaskTime() {
        return startTaskTime;
    }

    public OffsetDateTime getCompleteTaskTime() {
        return completeTaskTime;
    }

    @Override
    public NotificationMappingTelemetryModel obfuscate() {
        //TODO: Implement
        return null;
    }
}
