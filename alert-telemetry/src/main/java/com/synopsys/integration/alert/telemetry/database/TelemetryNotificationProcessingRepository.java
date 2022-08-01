package com.synopsys.integration.alert.telemetry.database;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryNotificationProcessingRepository extends JpaRepository<NotificationProcessingTelemetryEntity, UUID> {
}
