package com.synopsys.integration.alert.audit.mock;

import java.util.Date;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.mock.entity.MockEntityUtil;

public class MockAuditEntryEntity extends MockEntityUtil<AuditEntryEntity> {
    private Long commonConfigId;
    private Date timeCreated;
    private Date timeLastSent;
    private AuditEntryStatus status;
    private String errorMessage;
    private String errorStackTrace;
    private Long id;

    public MockAuditEntryEntity() {
        this(2L, new Date(400), new Date(500), AuditEntryStatus.SUCCESS, "errorMessage", "errorStackTrace", 1L);
    }

    private MockAuditEntryEntity(final Long commonConfigId, final Date timeCreated, final Date timeLastSent, final AuditEntryStatus status, final String errorMessage, final String errorStackTrace, final Long id) {
        super();
        this.commonConfigId = commonConfigId;
        this.timeCreated = timeCreated;
        this.timeLastSent = timeLastSent;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
        this.id = id;
    }

    public Long getCommonConfigId() {
        return commonConfigId;
    }

    public void setCommonConfigId(final Long commonConfigId) {
        this.commonConfigId = commonConfigId;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(final Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    public Date getTimeLastSent() {
        return timeLastSent;
    }

    public void setTimeLastSent(final Date timeLastSent) {
        this.timeLastSent = timeLastSent;
    }

    public AuditEntryStatus getStatus() {
        return status;
    }

    public void setStatus(final AuditEntryStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

    public void setErrorStackTrace(final String errorStackTrace) {
        this.errorStackTrace = errorStackTrace;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public AuditEntryEntity createEntity() {
        final AuditEntryEntity entity = new AuditEntryEntity(commonConfigId, timeCreated, timeLastSent, status.toString(), errorMessage, errorStackTrace);
        entity.setId(id);
        entity.setErrorMessage(errorMessage);
        entity.setErrorStackTrace(errorStackTrace);
        entity.setTimeLastSent(timeLastSent);
        return entity;
    }

    @Override
    public AuditEntryEntity createEmptyEntity() {
        return new AuditEntryEntity();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("commonConfigId", commonConfigId);
        json.addProperty("timeCreated", timeCreated.toLocaleString());
        json.addProperty("timeLastSent", timeLastSent.toLocaleString());
        json.addProperty("status", status.toString());
        json.addProperty("errorMessage", errorMessage);
        json.addProperty("errorStackTrace", errorStackTrace);
        json.addProperty("id", id);
        return json.toString();
    }

}
