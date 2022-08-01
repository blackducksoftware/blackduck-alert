/*
 * alert-telemetry
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.telemetry.database;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "telemetry_notification_mapping")
public class NotificationMappingTelemetryEntity extends BaseEntity {
    private static final long serialVersionUID = -456930081825295367L;
    @Id
    @Column(name = "correlation_id")
    private UUID correlationId;
    @Column(name = "start_task_time")
    private OffsetDateTime startTaskTime;
    @Column(name = "complete_task_time")
    private OffsetDateTime completeTaskTime;

    public NotificationMappingTelemetryEntity() {
    }

    public NotificationMappingTelemetryEntity(UUID correlationId, OffsetDateTime startTaskTime, OffsetDateTime completeTaskTime) {
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
}
