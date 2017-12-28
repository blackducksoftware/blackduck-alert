/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.datasource.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.blackducksoftware.integration.hub.alert.enumeration.StatusEnum;

@Entity
@Table(schema = "alert", name = "audit_entry")
public class AuditEntryEntity extends DatabaseEntity {
    private static final long serialVersionUID = -5848616198072005794L;

    @Column(name = "common_config_id")
    private Long commonConfigId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_created")
    private Date timeCreated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_last_sent")
    private Date timeLastSent;

    @Column(name = "status")
    private StatusEnum status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "error_stack_trace")
    private String errorStackTrace;

    public AuditEntryEntity() {

    }

    public AuditEntryEntity(final Long commonConfigId, final Date timeCreated, final Date timeLastSent, final StatusEnum status, final String errorMessage, final String errorStackTrace) {
        this.commonConfigId = commonConfigId;
        this.timeCreated = timeCreated;
        this.timeLastSent = timeLastSent;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
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

}
