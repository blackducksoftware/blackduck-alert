package com.blackduck.integration.alert.common.persistence.model;

import java.util.UUID;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class AuditJobStatusModel extends AlertSerializableModel {
    private UUID jobId;
    private String timeAuditCreated;
    private String timeLastSent;
    private String status;

    public AuditJobStatusModel() {
    }

    public AuditJobStatusModel(UUID jobId, String timeAuditCreated, String timeLastSent, String status) {
        this.jobId = jobId;
        this.timeAuditCreated = timeAuditCreated;
        this.timeLastSent = timeLastSent;
        this.status = status;
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getTimeAuditCreated() {
        return timeAuditCreated;
    }

    public String getTimeLastSent() {
        return timeLastSent;
    }

    public String getStatus() {
        return status;
    }

}
