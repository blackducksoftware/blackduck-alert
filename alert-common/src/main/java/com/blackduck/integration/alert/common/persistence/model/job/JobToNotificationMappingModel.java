/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job;

import java.util.UUID;

public class JobToNotificationMappingModel {
    private UUID correlationId;
    private UUID jobId;
    private Long notificationId;

    public JobToNotificationMappingModel(UUID correlationId, UUID jobId, Long notificationId) {
        this.correlationId = correlationId;
        this.jobId = jobId;
        this.notificationId = notificationId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Long getNotificationId() {
        return notificationId;
    }
}
