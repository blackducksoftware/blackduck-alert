package com.synopsys.integration.alert.common.persistence.model.job;

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
