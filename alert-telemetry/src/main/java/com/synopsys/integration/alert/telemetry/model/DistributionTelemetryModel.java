/*
 * alert-telemetry
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.telemetry.model;

/*
import java.time.OffsetDateTime;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class DistributionTelemetryModel extends AlertSerializableModel implements Obfuscated<DistributionTelemetryModel> {
    private static final long serialVersionUID = 233853562743312832L;
    private UUID eventId;
    private UUID jobId;
    private String eventDestination;
    private OffsetDateTime startTaskTime;
    private OffsetDateTime completeTaskTime;

    public DistributionTelemetryModel() {
    }

    public DistributionTelemetryModel(UUID eventId, UUID jobId, String eventDestination, OffsetDateTime startTaskTime, @Nullable OffsetDateTime completeTaskTime) {
        this.eventId = eventId;
        this.jobId = jobId;
        this.eventDestination = eventDestination;
        this.startTaskTime = startTaskTime;
        this.completeTaskTime = completeTaskTime;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getEventDestination() {
        return eventDestination;
    }

    public OffsetDateTime getStartTaskTime() {
        return startTaskTime;
    }

    public OffsetDateTime getCompleteTaskTime() {
        return completeTaskTime;
    }

    @Override
    public DistributionTelemetryModel obfuscate() {
        //TODO: Implement
        return null;
    }
}
*/

import java.time.OffsetDateTime;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class DistributionTelemetryModel extends BaseTelemetryModel implements Obfuscated<DistributionTelemetryModel> {
    private static final long serialVersionUID = 233853562743312832L;
    private UUID eventId;
    private UUID jobId;
    private String eventDestination;

    public DistributionTelemetryModel() {
    }

    public DistributionTelemetryModel(UUID eventId, UUID jobId, String eventDestination, OffsetDateTime startTaskTime, @Nullable OffsetDateTime completeTaskTime) {
        super(startTaskTime, completeTaskTime);
        this.eventId = eventId;
        this.jobId = jobId;
        this.eventDestination = eventDestination;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getEventDestination() {
        return eventDestination;
    }

    @Override
    public DistributionTelemetryModel obfuscate() {
        return null;
    }
}
