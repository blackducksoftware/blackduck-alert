package com.synopsys.integration.alert.telemetry.database.processing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryNotificationProcessingRepository extends JpaRepository<NotificationProcessingTelemetryEntity, NotificationProcessingTelemetryPK> {
}
