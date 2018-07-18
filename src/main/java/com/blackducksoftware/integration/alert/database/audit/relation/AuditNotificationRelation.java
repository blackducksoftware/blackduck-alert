/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.alert.database.audit.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.blackducksoftware.integration.alert.database.relation.DatabaseRelation;

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

    public AuditNotificationRelation() {
    }

    public AuditNotificationRelation(final Long auditEntryId, final Long notificationId) {
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

}
