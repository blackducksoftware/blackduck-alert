/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.audit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseRelation;
import com.synopsys.integration.alert.database.notification.NotificationEntity;

@Entity
@IdClass(AuditNotificationRelationPK.class)
@Table(schema = "alert", name = "audit_notification_relation")
public class AuditNotificationRelation extends DatabaseRelation {
    @Id
    @Column(name = "audit_entry_id")
    private Long auditEntryId;

    @Id
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "notification_id", referencedColumnName = "id", insertable = false, updatable = false)
    public NotificationEntity notificationContent;
    @ManyToOne
    @JoinColumn(name = "audit_entry_id", referencedColumnName = "id", insertable = false, updatable = false)
    private AuditEntryEntity auditEntryEntity;

    public AuditNotificationRelation() {
    }

    public AuditNotificationRelation(Long auditEntryId, Long notificationId) {
        super();
        this.auditEntryId = auditEntryId;
        this.notificationId = notificationId;
    }

    public Long getAuditEntryId() {
        return auditEntryId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public NotificationEntity getNotificationContent() {
        return notificationContent;
    }

    public AuditEntryEntity getAuditEntryEntity() {
        return auditEntryEntity;
    }
}
