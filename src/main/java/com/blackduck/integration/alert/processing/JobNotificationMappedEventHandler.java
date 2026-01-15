/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.processing;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.event.AlertEventHandler;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.event.JobNotificationMappedEvent;
import com.blackduck.integration.alert.api.processor.event.JobProcessingEvent;
import com.blackduck.integration.alert.common.logging.AlertLoggerFactory;
import com.blackduck.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;

@Component
public class JobNotificationMappedEventHandler implements AlertEventHandler<JobNotificationMappedEvent> {
    private final Logger notificationLogger = AlertLoggerFactory.getNotificationLogger(getClass());
    private final JobNotificationMappingAccessor jobMappingAccessor;
    private final EventManager eventManager;

    @Autowired
    public JobNotificationMappedEventHandler(JobNotificationMappingAccessor jobMappingAccessor, EventManager eventManager) {
        this.jobMappingAccessor = jobMappingAccessor;
        this.eventManager = eventManager;
    }

    @Override
    public void handle(JobNotificationMappedEvent event) {
        UUID correlationId = event.getCorrelationId();
        Set<UUID> jobConfigIds = jobMappingAccessor.getUniqueJobIds(correlationId);
        notificationLogger.debug("Discovered {} unique jobConfigId(s) for batch: {}", jobConfigIds.size(), correlationId);
        for (UUID jobConfigId : jobConfigIds) {
            notificationLogger.info("Creating processing event for jobConfigId: {}, batch: {}", jobConfigId, correlationId);
            eventManager.sendEvent(new JobProcessingEvent(correlationId, jobConfigId));
        }
    }
}
