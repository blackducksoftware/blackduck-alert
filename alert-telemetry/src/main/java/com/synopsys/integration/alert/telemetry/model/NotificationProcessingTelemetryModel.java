package com.synopsys.integration.alert.telemetry.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class NotificationProcessingTelemetryModel extends AlertSerializableModel implements Obfuscated<NotificationMappingTelemetryModel> {
    private static final long serialVersionUID = -6704891387608183164L;
    private UUID correlationId;
    private UUID eventId;
    private OffsetDateTime startTaskTime;
    private OffsetDateTime completeTaskTime;

    public NotificationProcessingTelemetryModel(UUID correlationId, UUID eventId, OffsetDateTime startTaskTime, @Nullable OffsetDateTime completeTaskTime) {
        this.correlationId = correlationId;
        this.eventId = eventId;
        this.startTaskTime = startTaskTime;
        this.completeTaskTime = completeTaskTime;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public UUID getCorrelationId() {
        return correlationId;
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
    public NotificationMappingTelemetryModel obfuscate() {
        //TODO: Implement
        return null;
    }
}
