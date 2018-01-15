package com.blackducksoftware.integration.hub.alert.audit.mock;

import java.util.Date;

import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.enumeration.StatusEnum;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.google.gson.JsonObject;

public class MockAuditEntryEntity extends MockEntityUtil<AuditEntryEntity> {
    private Long commonConfigId;
    private Date timeCreated;
    private Date timeLastSent;
    private StatusEnum status;
    private String errorMessage;
    private String errorStackTrace;
    private Long id;

    public MockAuditEntryEntity() {
        this(2L, new Date(400), new Date(500), StatusEnum.SUCCESS, "errorMessage", "errorStackTrace", 1L);
    }

    private MockAuditEntryEntity(final Long commonConfigId, final Date timeCreated, final Date timeLastSent, final StatusEnum status, final String errorMessage, final String errorStackTrace, final Long id) {
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

    public Date getTimeCreated() {
        return timeCreated;
    }

    public Date getTimeLastSent() {
        return timeLastSent;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

    public void setCommonConfigId(final Long commonConfigId) {
        this.commonConfigId = commonConfigId;
    }

    public void setTimeCreated(final Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    public void setTimeLastSent(final Date timeLastSent) {
        this.timeLastSent = timeLastSent;
    }

    public void setStatus(final StatusEnum status) {
        this.status = status;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setErrorStackTrace(final String errorStackTrace) {
        this.errorStackTrace = errorStackTrace;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public AuditEntryEntity createEntity() {
        final AuditEntryEntity entity = new AuditEntryEntity(commonConfigId, timeCreated, timeLastSent, status, errorMessage, errorStackTrace);
        entity.setId(id);
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
        json.addProperty("status", status.name());
        json.addProperty("errorMessage", errorMessage);
        json.addProperty("errorStackTrace", errorStackTrace);
        json.addProperty("id", id);
        return json.toString();
    }

}
