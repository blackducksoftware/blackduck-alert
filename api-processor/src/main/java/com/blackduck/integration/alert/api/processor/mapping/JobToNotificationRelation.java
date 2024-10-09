package com.blackduck.integration.alert.api.processor.mapping;

import java.util.UUID;

public class JobToNotificationRelation {
    private final UUID correlationId;
    private final UUID jobId;
    private final Long notificationId;

    public JobToNotificationRelation(UUID correlationId, UUID jobId, Long notificationId) {
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
