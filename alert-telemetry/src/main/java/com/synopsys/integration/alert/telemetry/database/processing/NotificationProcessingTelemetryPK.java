package com.synopsys.integration.alert.telemetry.database.processing;

import java.io.Serializable;
import java.util.UUID;

public class NotificationProcessingTelemetryPK implements Serializable {
    private static final long serialVersionUID = 8170515647368779136L;

    private UUID correlationId;
    private UUID jobId;

    public NotificationProcessingTelemetryPK() {
    }

    public NotificationProcessingTelemetryPK(UUID correlationId, UUID jobId) {
        this.correlationId = correlationId;
        this.jobId = jobId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }
}
