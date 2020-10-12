package com.synopsys.integration.alert.component.audit.mock;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.mock.entity.MockEntityUtil;

public class MockAuditEntryEntity extends MockEntityUtil<AuditEntryEntity> {
    private UUID commonConfigId = UUID.fromString("1c0c7769-7cae-47d1-b80f-8c09eb8b90b9");
    private OffsetDateTime timeLastSent = OffsetDateTime.of(2000, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC);
    private OffsetDateTime timeCreated = timeLastSent.plusMinutes(1);
    private AuditEntryStatus status = AuditEntryStatus.SUCCESS;
    private String errorMessage = "errorMessage";
    private String errorStackTrace = "errorStackTrace";
    private Long id = 1L;

    public UUID getCommonConfigId() {
        return commonConfigId;
    }

    public void setCommonConfigId(UUID commonConfigId) {
        this.commonConfigId = commonConfigId;
    }

    public OffsetDateTime getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(OffsetDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    public OffsetDateTime getTimeLastSent() {
        return timeLastSent;
    }

    public void setTimeLastSent(OffsetDateTime timeLastSent) {
        this.timeLastSent = timeLastSent;
    }

    public AuditEntryStatus getStatus() {
        return status;
    }

    public void setStatus(AuditEntryStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

    public void setErrorStackTrace(String errorStackTrace) {
        this.errorStackTrace = errorStackTrace;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public AuditEntryEntity createEntity() {
        AuditEntryEntity entity = new AuditEntryEntity(commonConfigId, timeCreated, timeLastSent, status.toString(), errorMessage, errorStackTrace);
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

    @Override
    public String getEntityJson() {
        JsonObject json = new JsonObject();
        json.addProperty("commonConfigId", commonConfigId.toString());
        json.addProperty("timeCreated", timeCreated.toString());
        json.addProperty("timeLastSent", timeLastSent.toString());
        json.addProperty("status", status.toString());
        json.addProperty("errorMessage", errorMessage);
        json.addProperty("errorStackTrace", errorStackTrace);
        json.addProperty("id", id);
        return json.toString();
    }

}
