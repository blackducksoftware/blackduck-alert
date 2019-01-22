package com.synopsys.integration.alert.audit.mock;

import java.util.Date;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.mock.entity.MockEntityUtil;

public class MockAuditEntryEntity extends MockEntityUtil<AuditEntryEntity> {
    private UUID commonConfigId = UUID.fromString("1c0c7769-7cae-47d1-b80f-8c09eb8b90b9");
    private Date timeCreated = new Date(400);
    private Date timeLastSent = new Date(500);
    private AuditEntryStatus status = AuditEntryStatus.SUCCESS;
    private String errorMessage = "errorMessage";
    private String errorStackTrace = "errorStackTrace";
    private Long id = 1L;

    public UUID getCommonConfigId() {
        return commonConfigId;
    }

    public void setCommonConfigId(final UUID commonConfigId) {
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
        json.addProperty("commonConfigId", commonConfigId.toString());
        json.addProperty("timeCreated", timeCreated.toLocaleString());
        json.addProperty("timeLastSent", timeLastSent.toLocaleString());
        json.addProperty("status", status.toString());
        json.addProperty("errorMessage", errorMessage);
        json.addProperty("errorStackTrace", errorStackTrace);
        json.addProperty("id", id);
        return json.toString();
    }

}
