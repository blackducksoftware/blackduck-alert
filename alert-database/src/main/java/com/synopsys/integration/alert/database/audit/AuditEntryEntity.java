/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "audit_entries")
public class AuditEntryEntity extends DatabaseEntity {
    public static final int STACK_TRACE_CHAR_LIMIT = 10000;

    @Column(name = "common_config_id")
    private UUID commonConfigId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_created")
    private Date timeCreated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_last_sent")
    private Date timeLastSent;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "error_stack_trace", length = STACK_TRACE_CHAR_LIMIT)
    private String errorStackTrace;

    @OneToMany(mappedBy = "auditEntryEntity")
    private List<AuditNotificationRelation> auditNotificationRelations;

    public AuditEntryEntity() {
        // JPA requires default constructor definitions
    }

    public AuditEntryEntity(final UUID commonConfigId, final Date timeCreated, final Date timeLastSent, final String status, final String errorMessage, final String errorStackTrace) {
        this.commonConfigId = commonConfigId;
        this.timeCreated = timeCreated;
        this.timeLastSent = timeLastSent;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

    public UUID getCommonConfigId() {
        return commonConfigId;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public Date getTimeLastSent() {
        return timeLastSent;
    }

    public void setTimeLastSent(final Date timeLastSent) {
        this.timeLastSent = timeLastSent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
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

    public List<AuditNotificationRelation> getAuditNotificationRelations() {
        return auditNotificationRelations;
    }
}
