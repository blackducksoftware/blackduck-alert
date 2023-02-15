package com.synopsys.integration.alert.common.persistence.model.job.workflow;

import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class JobSubTaskStatusModel extends AlertSerializableModel {
    private static final long serialVersionUID = -9192758155312295976L;
    private final UUID parentEventId;
    private final UUID jobId;
    private final UUID jobExecutionId;
    private final Long remainingTaskCount;
    private final UUID notificationCorrelationId;

    public JobSubTaskStatusModel(UUID parentEventId, UUID jobId, UUID jobExecutionId, Long remainingTaskCount, UUID notificationCorrelationId) {
        this.parentEventId = parentEventId;
        this.jobId = jobId;
        this.jobExecutionId = jobExecutionId;
        this.remainingTaskCount = remainingTaskCount;
        this.notificationCorrelationId = notificationCorrelationId;
    }

    public UUID getParentEventId() {
        return parentEventId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public UUID getJobExecutionId() {
        return jobExecutionId;
    }

    public Long getRemainingTaskCount() {
        return remainingTaskCount;
    }

    public UUID getNotificationCorrelationId() {
        return notificationCorrelationId;
    }
}
