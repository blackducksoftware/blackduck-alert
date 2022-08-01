package com.synopsys.integration.alert.telemetry.database;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "telemetry_notification_processing")
public class NotificationProcessingTelemetryEntity extends BaseEntity {
    private static final long serialVersionUID = -2972703802600332023L;
    @Id
    @Column(name = "correlation_id")
    private UUID correlationId;
    @Column(name = "job_id")
    private UUID jobId;
    @Column(name = "start_task_time")
    private OffsetDateTime startTaskTime;
    @Column(name = "complete_task_time")
    private OffsetDateTime completeTaskTime;

    public NotificationProcessingTelemetryEntity() {
    }

    public NotificationProcessingTelemetryEntity(UUID correlationId, UUID jobId, OffsetDateTime startTaskTime, OffsetDateTime completeTaskTime) {
        this.correlationId = correlationId;
        this.jobId = jobId;
        this.startTaskTime = startTaskTime;
        this.completeTaskTime = completeTaskTime;
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
}
