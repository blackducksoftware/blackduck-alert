/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
