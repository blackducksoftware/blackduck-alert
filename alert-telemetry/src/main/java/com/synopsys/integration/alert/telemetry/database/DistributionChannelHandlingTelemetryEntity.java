package com.synopsys.integration.alert.telemetry.database;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "telemetry_distribution_channel_handling")
public class DistributionChannelHandlingTelemetryEntity extends BaseEntity {
    private static final long serialVersionUID = -2951336014837421817L;
    
    @Id
    @Column(name = "event_id")
    private UUID eventId;
    @Column(name = "job_id")
    private UUID jobId;
    @Column(name = "event_destination")
    private String eventDestination;
    @Column(name = "start_task_time")
    private OffsetDateTime startTaskTime;
    @Column(name = "complete_task_time")
    private OffsetDateTime completeTaskTime;

    public DistributionChannelHandlingTelemetryEntity() {
    }

    public DistributionChannelHandlingTelemetryEntity(UUID eventId, UUID jobId, String eventDestination, OffsetDateTime startTaskTime, OffsetDateTime completeTaskTime) {
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
}
