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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.TelemetryAccessor;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.telemetry.model.DistributionTelemetryModel;
import com.synopsys.integration.alert.telemetry.model.NotificationMappingTelemetryModel;
import com.synopsys.integration.alert.telemetry.model.NotificationProcessingTelemetryModel;

@Component
public class DefaultTelemetryAccessor implements TelemetryAccessor {
    private final MockTelemetryRepository mockTelemetryRepository;
    private final TelemetryNotificationMappingRepository telemetryNotificationMappingRepository;
    private final TelemetryNotificationProcessingRepository telemetryNotificationProcessingRepository;

    @Autowired
    public DefaultTelemetryAccessor(
        TelemetryNotificationMappingRepository telemetryNotificationMappingRepository,
        MockTelemetryRepository mockTelemetryRepository,
        TelemetryNotificationProcessingRepository telemetryNotificationProcessingRepository
    ) {
        this.mockTelemetryRepository = mockTelemetryRepository;
        this.telemetryNotificationMappingRepository = telemetryNotificationMappingRepository;
        this.telemetryNotificationProcessingRepository = telemetryNotificationProcessingRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NotificationMappingTelemetryModel createNotificationMappingTelemetryTask(UUID correlationId) {
        NotificationMappingTelemetryEntity notificationMappingTelemetryEntity = new NotificationMappingTelemetryEntity(correlationId, DateUtils.createCurrentDateTimestamp(), null);
        NotificationMappingTelemetryEntity savedTelemetryEntity = telemetryNotificationMappingRepository.save(notificationMappingTelemetryEntity);
        return new NotificationMappingTelemetryModel(
            savedTelemetryEntity.getCorrelationId(),
            savedTelemetryEntity.getStartTaskTime(),
            savedTelemetryEntity.getCompleteTaskTime()
        );
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NotificationMappingTelemetryModel completeNotificationMappingTelemetryTask(UUID correlationId) {
        NotificationMappingTelemetryEntity savedTelemetryEntity = telemetryNotificationMappingRepository.getById(correlationId);
        NotificationMappingTelemetryEntity updatedTelemetryEntity = new NotificationMappingTelemetryEntity(
            correlationId,
            savedTelemetryEntity.getStartTaskTime(),
            DateUtils.createCurrentDateTimestamp()
        );
        updatedTelemetryEntity = telemetryNotificationMappingRepository.save(updatedTelemetryEntity);
        return new NotificationMappingTelemetryModel(
            updatedTelemetryEntity.getCorrelationId(),
            updatedTelemetryEntity.getStartTaskTime(),
            updatedTelemetryEntity.getCompleteTaskTime()
        );
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NotificationProcessingTelemetryModel createNotificationProcessingTelemetryTask(UUID correlationId, UUID jobId) {
        NotificationProcessingTelemetryEntity notificationProcessingTelemetryEntity = new NotificationProcessingTelemetryEntity(
            correlationId,
            jobId,
            DateUtils.createCurrentDateTimestamp(),
            null
        );
        NotificationProcessingTelemetryEntity savedTelemetryEntity = telemetryNotificationProcessingRepository.save(notificationProcessingTelemetryEntity);
        return new NotificationProcessingTelemetryModel(
            savedTelemetryEntity.getCorrelationId(),
            savedTelemetryEntity.getJobId(),
            savedTelemetryEntity.getStartTaskTime(),
            savedTelemetryEntity.getCompleteTaskTime()
        );
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NotificationProcessingTelemetryModel completeNotificationProcessingTelemetryTask(UUID correlationId) {
        NotificationProcessingTelemetryEntity savedTelemetryEntity = telemetryNotificationProcessingRepository.getById(correlationId);
        NotificationProcessingTelemetryEntity updatedTelemetryEntity = new NotificationProcessingTelemetryEntity(
            correlationId,
            savedTelemetryEntity.getJobId(),
            savedTelemetryEntity.getStartTaskTime(),
            DateUtils.createCurrentDateTimestamp()
        );
        updatedTelemetryEntity = telemetryNotificationProcessingRepository.save(updatedTelemetryEntity);
        return new NotificationProcessingTelemetryModel(
            updatedTelemetryEntity.getCorrelationId(),
            updatedTelemetryEntity.getJobId(),
            updatedTelemetryEntity.getStartTaskTime(),
            updatedTelemetryEntity.getCompleteTaskTime()
        );
    }

    public DistributionTelemetryModel createDistributionTelemetryTask(UUID jobId) {
        DistributionTelemetryModel distributionTelemetryModel = new DistributionTelemetryModel(jobId, DateUtils.createCurrentDateTimestamp(), null);
        return mockTelemetryRepository.saveDistributionEvent(jobId, distributionTelemetryModel);
    }

    public DistributionTelemetryModel completeDistributionTelemetryTask(UUID jobId) {
        DistributionTelemetryModel distributionTelemetryModel = mockTelemetryRepository.getOneDistributionEvent(jobId);
        DistributionTelemetryModel completeDistributionTelemetryTask = new DistributionTelemetryModel(
            jobId,
            distributionTelemetryModel.getStartTaskTime(),
            DateUtils.createCurrentDateTimestamp()
        );
        return mockTelemetryRepository.saveDistributionEvent(jobId, completeDistributionTelemetryTask);
    }
}
