/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.database.audit.relation;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.entity.configuration.ConfigGroupEntity;
import com.synopsys.integration.alert.database.relation.DatabaseRelation;

@Entity
@IdClass(AuditJobRelationPK.class)
@Table(schema = "alert", name = "audit_job_relation")
public class AuditJobRelation extends DatabaseRelation {
    @Id
    @Column(name = "audit_id")
    private Long auditId;

    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "JOB_ID", insertable = false, updatable = false)
    public ConfigGroupEntity configGroupEntity;

    @ManyToOne
    @JoinColumn(name = "audit_id", referencedColumnName = "id", insertable = false, updatable = false)
    private AuditEntryEntity auditEntryEntity;

    public AuditJobRelation() {
        // JPA requires default constructor definitions
    }

    public AuditJobRelation(final Long auditId, final UUID jobId) {
        this.auditId = auditId;
        this.jobId = jobId;
    }

    public Long getAuditId() {
        return auditId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public ConfigGroupEntity getConfigGroupEntity() {
        return configGroupEntity;
    }

}
