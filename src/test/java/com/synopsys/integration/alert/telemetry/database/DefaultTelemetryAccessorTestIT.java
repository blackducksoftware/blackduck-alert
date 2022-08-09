package com.synopsys.integration.alert.telemetry.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class DefaultTelemetryAccessorTestIT {
    @Autowired
    private DefaultTelemetryAccessor telemetryAccessor;

    @AfterEach
    public void cleanup() {
        telemetryAccessor.deleteNotificationMappingTelemetryCreatedBefore(OffsetDateTime.now());
        telemetryAccessor.deleteDistributionTelemetryCreatedBefore(OffsetDateTime.now());
    }

    @Test
    void testCascadeDelete() {
        createTelemetryData();
        assertEquals(1, telemetryAccessor.countNotificationMappingTelemetryTasks());
        assertEquals(1, telemetryAccessor.countNotificationProcessingTelemetryTasks());
        assertEquals(1, telemetryAccessor.countDistributionHandlingTelemetryTasks());

        telemetryAccessor.deleteNotificationMappingTelemetryCreatedBefore(OffsetDateTime.now());
        telemetryAccessor.deleteDistributionTelemetryCreatedBefore(OffsetDateTime.now());

        assertEquals(0, telemetryAccessor.countNotificationMappingTelemetryTasks());
        assertEquals(0, telemetryAccessor.countNotificationProcessingTelemetryTasks());
        assertEquals(0, telemetryAccessor.countDistributionHandlingTelemetryTasks());
    }

    private void createTelemetryData() {
        UUID correlationId = UUID.randomUUID();
        telemetryAccessor.createNotificationMappingTelemetryTask(correlationId);
        telemetryAccessor.completeNotificationMappingTelemetryTask(correlationId);

        UUID jobId = UUID.randomUUID();
        telemetryAccessor.createNotificationProcessingTelemetryTask(correlationId, jobId);
        telemetryAccessor.completeNotificationProcessingTelemetryTask(correlationId, jobId);

        UUID eventId = UUID.randomUUID();
        telemetryAccessor.createDistributionTelemetryTask(eventId, jobId, "testDestination");
        telemetryAccessor.completeDistributionTelemetryTask(eventId);
    }
}
