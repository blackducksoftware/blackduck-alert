package com.synopsys.integration.alert.telemetry.database;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryDistributionChannelHandlingRepository extends JpaRepository<DistributionChannelHandlingTelemetryEntity, UUID> {
}
