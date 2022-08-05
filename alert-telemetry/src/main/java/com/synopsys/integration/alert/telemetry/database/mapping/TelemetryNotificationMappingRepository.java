/*
 * alert-telemetry
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.telemetry.database.mapping;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryNotificationMappingRepository extends JpaRepository<NotificationMappingTelemetryEntity, UUID> {
}
