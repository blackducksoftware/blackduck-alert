package com.synopsys.integration.alert.database.audit;

import java.io.Serializable;
import java.util.UUID;

public class AuditFailedNotificationRelationPK implements Serializable {
    private UUID failedAuditEntryId;
    private Long notificationId;

    public AuditFailedNotificationRelationPK() {
    }

    public UUID getFailedAuditEntryId() {
        return failedAuditEntryId;
    }

    public void setFailedAuditEntryId(final UUID failedAuditEntryId) {
        this.failedAuditEntryId = failedAuditEntryId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(final Long notificationId) {
        this.notificationId = notificationId;
    }
}
