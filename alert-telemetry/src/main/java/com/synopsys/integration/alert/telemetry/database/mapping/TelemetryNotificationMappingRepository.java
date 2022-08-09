/*
 * alert-telemetry
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.telemetry.database.mapping;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TelemetryNotificationMappingRepository extends JpaRepository<NotificationMappingTelemetryEntity, UUID> {
    @Query("DELETE FROM NotificationMappingTelemetryEntity entity"
        + " WHERE entity.startTaskTime < :date"
    )
    @Modifying
    int bulkDeleteCreatedAtBefore(@Param("date") OffsetDateTime date);
}
