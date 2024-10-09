package com.blackduck.integration.alert.common.rest.model;

import com.blackduck.integration.alert.common.persistence.model.AuditJobStatusModel;

public class JobAuditModel extends Config {
    private String configId;
    private String name;
    private String eventType;
    private AuditJobStatusModel auditJobStatusModel;
    private String errorMessage;
    private String errorStackTrace;

    public JobAuditModel() {
    }

    public JobAuditModel(final String id, final String configId, final String name, final String eventType, final AuditJobStatusModel auditJobStatusModel, final String errorMessage, final String errorStackTrace) {
        super(id);
        this.configId = configId;
        this.name = name;
        this.eventType = eventType;
        this.auditJobStatusModel = auditJobStatusModel;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

    public String getConfigId() {
        return configId;
    }

    public String getName() {
        return name;
    }

    public String getEventType() {
        return eventType;
    }

    public AuditJobStatusModel getAuditJobStatusModel() {
        return auditJobStatusModel;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

}
