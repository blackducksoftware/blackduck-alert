/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.audit;

import java.io.Serializable;

public class AuditNotificationRelationPK implements Serializable {
    private static final long serialVersionUID = -9015966905838645720L;
    private Long auditEntryId;
    private Long notificationId;

    public AuditNotificationRelationPK() {
        // JPA requires default constructor definitions
    }

    public Long getAuditEntryId() {
        return auditEntryId;
    }

    public void setAuditEntryId(Long auditEntryId) {
        this.auditEntryId = auditEntryId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
}
