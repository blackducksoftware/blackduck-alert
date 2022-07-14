/*
 * alert-telemetry
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.telemetry.database;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.telemetry.model.NotificationMappingTelemetryModel;
import com.synopsys.integration.alert.telemetry.model.NotificationProcessingTelemetryModel;

//TODO: make this Default, implement a TelemtryAccessor in alert-common
// make these transactional
@Component
public class TelemetryAccessor {
    private final TelemetryRepository telemetryRepository;

    @Autowired
    public TelemetryAccessor(TelemetryRepository telemetryRepository) {
        this.telemetryRepository = telemetryRepository;
    }

    public NotificationMappingTelemetryModel createNotificationMappingTelemetryTask(UUID correlationId) {
        NotificationMappingTelemetryModel notificationMappingTelemetryModel = new NotificationMappingTelemetryModel(correlationId, DateUtils.createCurrentDateTimestamp(), null);
        return telemetryRepository.saveMapping(correlationId, notificationMappingTelemetryModel);
    }

    public NotificationMappingTelemetryModel completeNotificationMappingTelemetryTask(UUID correlationId) {
        NotificationMappingTelemetryModel notificationMappingTelemetryModel = telemetryRepository.getOneMapping(correlationId);
        NotificationMappingTelemetryModel completedNotificationMappingTelemetryModel = new NotificationMappingTelemetryModel(
            correlationId,
            notificationMappingTelemetryModel.getStartTaskTime(),
            DateUtils.createCurrentDateTimestamp()
        );
        return telemetryRepository.saveMapping(correlationId, completedNotificationMappingTelemetryModel);
    }

    public NotificationProcessingTelemetryModel createNotificationProcessingTelemetryTask(UUID correlationId, UUID eventId) {
        NotificationProcessingTelemetryModel notificationProcessingTelemetryModel = new NotificationProcessingTelemetryModel(
            correlationId,
            eventId,
            DateUtils.createCurrentDateTimestamp(),
            null
        );
        return telemetryRepository.saveProcessing(correlationId, notificationProcessingTelemetryModel);
    }

    public NotificationProcessingTelemetryModel completeNotificationProcessingTelemetryTask(UUID correlationId) {
        NotificationProcessingTelemetryModel notificationProcessingTelemetryModel = telemetryRepository.getOneProcessing(correlationId);
        NotificationProcessingTelemetryModel completedNotificationProcessingTelemetryTask = new NotificationProcessingTelemetryModel(
            correlationId,
            notificationProcessingTelemetryModel.getEventId(),
            notificationProcessingTelemetryModel.getStartTaskTime(),
            DateUtils.createCurrentDateTimestamp()
        );
        return telemetryRepository.saveProcessing(correlationId, completedNotificationProcessingTelemetryTask);
    }
}
