/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processing;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.processor.api.event.JobNotificationMappedEvent;
import com.synopsys.integration.alert.processor.api.event.JobProcessingEvent;
import com.synopsys.integration.alert.processor.api.mapping.JobNotificationMap;

@Component
public class JobNotificationMappedEventHandler implements AlertEventHandler<JobNotificationMappedEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Logger notificationLogger = AlertLoggerFactory.getNotificationLogger(getClass());
    private final JobNotificationMap jobNotificationMap;
    private final EventManager eventManager;

    @Autowired
    public JobNotificationMappedEventHandler(JobNotificationMap jobNotificationMap, EventManager eventManager) {
        this.jobNotificationMap = jobNotificationMap;
        this.eventManager = eventManager;
    }

    @Override
    public void handle(JobNotificationMappedEvent event) {
        Set<UUID> jobIds = jobNotificationMap.getJobIds();
        logger.debug("Job Ids: {}", jobIds);
        UUID correlationId = event.getCorrelationId();
        for (UUID job : jobIds) {
            List<Long> notificationIds = jobNotificationMap.getNotificationsForJob(event.getCorrelationId(), job);
            eventManager.sendEvent(new JobProcessingEvent(correlationId, job));
            String joinedIds = StringUtils.join(notificationIds, ", ");
            notificationLogger.debug("Notifications mapped to job: CorrelationID: {}, JobID: {}, Notifications: {}", correlationId, job, joinedIds);
        }
    }
}
