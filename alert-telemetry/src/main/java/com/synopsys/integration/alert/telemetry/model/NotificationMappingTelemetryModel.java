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

import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class NotificationMappingTelemetryModel extends BaseTelemetryModel implements Obfuscated<NotificationMappingTelemetryModel> {
    private static final long serialVersionUID = 1131364695726016688L;
    private UUID correlationId;

    public NotificationMappingTelemetryModel() {
        // For serialization
    }

    public NotificationMappingTelemetryModel(UUID correlationId, OffsetDateTime startTaskTime, @Nullable OffsetDateTime completeTaskTime) {
        super(startTaskTime, completeTaskTime);
        this.correlationId = correlationId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    @Override
    public NotificationMappingTelemetryModel obfuscate() {
        //TODO: Implement
        return null;
    }
}
