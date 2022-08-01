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

public class NotificationProcessingTelemetryModel extends AlertSerializableModel implements Obfuscated<NotificationMappingTelemetryModel> {
    private static final long serialVersionUID = -6704891387608183164L;
    private UUID correlationId;
    private UUID jobId;
    private OffsetDateTime startTaskTime;
    private OffsetDateTime completeTaskTime;

    public NotificationProcessingTelemetryModel(UUID correlationId, UUID jobId, OffsetDateTime startTaskTime, @Nullable OffsetDateTime completeTaskTime) {
        this.correlationId = correlationId;
        this.jobId = jobId;
        this.startTaskTime = startTaskTime;
        this.completeTaskTime = completeTaskTime;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public UUID getJobId() {
        return jobId;
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
