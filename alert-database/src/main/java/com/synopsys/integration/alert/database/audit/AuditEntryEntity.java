/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.audit;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "audit_entries")
public class AuditEntryEntity extends BaseEntity implements DatabaseEntity {
    public static final int STACK_TRACE_CHAR_LIMIT = 10000;
    @Id
    @GeneratedValue(generator = "alert.audit_entries_id_seq_generator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "alert.audit_entries_id_seq_generator", sequenceName = "alert.audit_entries_id_seq")
    @Column(name = "id")
    private Long id;

    // TODO rename to jobId
    @Column(name = "common_config_id")
    private UUID commonConfigId;

    @Column(name = "time_created")
    private OffsetDateTime timeCreated;

    @Column(name = "time_last_sent")
    private OffsetDateTime timeLastSent;

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

    public AuditEntryEntity(UUID commonConfigId, OffsetDateTime timeCreated, OffsetDateTime timeLastSent, String status, String errorMessage, String errorStackTrace) {
        this.commonConfigId = commonConfigId;
        this.timeCreated = timeCreated;
        this.timeLastSent = timeLastSent;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public UUID getCommonConfigId() {
        return commonConfigId;
    }

    public OffsetDateTime getTimeCreated() {
        return timeCreated;
    }

    public OffsetDateTime getTimeLastSent() {
        return timeLastSent;
    }

    public void setTimeLastSent(OffsetDateTime timeLastSent) {
        this.timeLastSent = timeLastSent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public List<AuditNotificationRelation> getAuditNotificationRelations() {
        return auditNotificationRelations;
    }

}
