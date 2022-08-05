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
    private final TelemetryNotificationMappingRepository telemetryNotificationMappingRepository;
    private final TelemetryNotificationProcessingRepository telemetryNotificationProcessingRepository;
    private final TelemetryDistributionChannelHandlingRepository telemetryDistributionChannelHandlingRepository;

    @Autowired
    public DefaultTelemetryAccessor(
        TelemetryNotificationMappingRepository telemetryNotificationMappingRepository,
        TelemetryNotificationProcessingRepository telemetryNotificationProcessingRepository,
        TelemetryDistributionChannelHandlingRepository telemetryDistributionChannelHandlingRepository
    ) {
        this.telemetryNotificationMappingRepository = telemetryNotificationMappingRepository;
        this.telemetryNotificationProcessingRepository = telemetryNotificationProcessingRepository;
        this.telemetryDistributionChannelHandlingRepository = telemetryDistributionChannelHandlingRepository;
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
            savedTelemetryEntity.getCorrelationId(),
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
    public NotificationProcessingTelemetryModel completeNotificationProcessingTelemetryTask(UUID correlationId, UUID jobId) {
        NotificationProcessingTelemetryPK notificationProcessingTelemetryPK = new NotificationProcessingTelemetryPK(correlationId, jobId);
        NotificationProcessingTelemetryEntity savedTelemetryEntity = telemetryNotificationProcessingRepository.getById(notificationProcessingTelemetryPK);
        NotificationProcessingTelemetryEntity updatedTelemetryEntity = new NotificationProcessingTelemetryEntity(
            savedTelemetryEntity.getCorrelationId(),
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

    @Transactional(propagation = Propagation.REQUIRED)
    public DistributionTelemetryModel createDistributionTelemetryTask(UUID eventId, UUID jobId, String eventDestination) {
        DistributionChannelHandlingTelemetryEntity distributionChannelHandlingTelemetryEntity = new DistributionChannelHandlingTelemetryEntity(
            eventId,
            jobId,
            eventDestination,
            DateUtils.createCurrentDateTimestamp(),
            null
        );
        DistributionChannelHandlingTelemetryEntity savedTelemetryEntity = telemetryDistributionChannelHandlingRepository.save(distributionChannelHandlingTelemetryEntity);
        return new DistributionTelemetryModel(
            savedTelemetryEntity.getEventId(),
            savedTelemetryEntity.getJobId(),
            savedTelemetryEntity.getEventDestination(),
            savedTelemetryEntity.getStartTaskTime(),
            savedTelemetryEntity.getCompleteTaskTime()
        );
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DistributionTelemetryModel completeDistributionTelemetryTask(UUID eventId) {
        DistributionChannelHandlingTelemetryEntity savedTelemetryEntity = telemetryDistributionChannelHandlingRepository.getById(eventId);
        DistributionChannelHandlingTelemetryEntity updatedTelemetryEntity = new DistributionChannelHandlingTelemetryEntity(
            savedTelemetryEntity.getEventId(),
            savedTelemetryEntity.getJobId(),
            savedTelemetryEntity.getEventDestination(),
            savedTelemetryEntity.getStartTaskTime(),
            DateUtils.createCurrentDateTimestamp()
        );
        updatedTelemetryEntity = telemetryDistributionChannelHandlingRepository.save(updatedTelemetryEntity);
        return new DistributionTelemetryModel(
            updatedTelemetryEntity.getEventId(),
            updatedTelemetryEntity.getJobId(),
            updatedTelemetryEntity.getEventDestination(),
            updatedTelemetryEntity.getStartTaskTime(),
            updatedTelemetryEntity.getCompleteTaskTime()
        );
    }
}
