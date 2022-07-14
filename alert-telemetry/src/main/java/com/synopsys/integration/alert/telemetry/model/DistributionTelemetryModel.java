package com.synopsys.integration.alert.telemetry.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class DistributionTelemetryModel extends AlertSerializableModel implements Obfuscated<DistributionTelemetryModel> {
    private static final long serialVersionUID = 233853562743312832L;
    private UUID eventId;
    private OffsetDateTime startTaskTime;
    private OffsetDateTime completeTaskTime;

    public DistributionTelemetryModel(UUID eventId, OffsetDateTime startTaskTime, @Nullable OffsetDateTime completeTaskTime) {
        this.eventId = eventId;
        this.startTaskTime = startTaskTime;
        this.completeTaskTime = completeTaskTime;
    }

    public UUID getEventId() {
        return eventId;
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
